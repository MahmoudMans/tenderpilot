package com.tenderpilot.rag.application;

import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidworkspace.application.BidDocumentStorageGateway;

import com.tenderpilot.bidworkspace.domain.BidDocumentEntity;

import com.tenderpilot.bidworkspace.infrastructure.BidDocumentRepository;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.localai.application.BidDocumentTextExtractor;

import com.tenderpilot.localai.domain.BidDocumentExtractionResult;
import com.tenderpilot.localai.domain.BidDocumentTextEntity;

import com.tenderpilot.localai.infrastructure.BidDocumentTextRepository;
import com.tenderpilot.localai.infrastructure.LocalAiSettings;

import com.tenderpilot.matching.domain.TenderMatchEntity;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import com.tenderpilot.rag.api.AskBidDocumentsRequest;
import com.tenderpilot.rag.api.BidRagAnswerResponse;
import com.tenderpilot.rag.api.BidRagIndexStatusResponse;
import com.tenderpilot.rag.api.BidRagRebuildResponse;
import com.tenderpilot.rag.api.RagStatusResponse;

import com.tenderpilot.rag.domain.BidRagChunk;
import com.tenderpilot.rag.domain.BidRagChunkEntity;
import com.tenderpilot.rag.domain.BidRagCitationValue;
import com.tenderpilot.rag.domain.BidRagQueryEntity;
import com.tenderpilot.rag.domain.OllamaRagAnswerResult;
import com.tenderpilot.rag.domain.RagContextItem;

import com.tenderpilot.rag.infrastructure.BidRagChunkRepository;
import com.tenderpilot.rag.infrastructure.BidRagQueryRepository;
import com.tenderpilot.rag.infrastructure.RagEmbeddingCodec;
import com.tenderpilot.rag.infrastructure.RagSettings;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BidRagService {

    private static final int EMBEDDING_BATCH_SIZE =
            32;


    private final TenderMatchRepository
            matchRepository;


    private final BidDecisionRepository
            decisionRepository;


    private final BidDocumentRepository
            documentRepository;


    private final BidDocumentTextRepository
            textRepository;


    private final BidRagChunkRepository
            chunkRepository;


    private final BidRagQueryRepository
            queryRepository;


    private final BidDocumentStorageGateway
            storageGateway;


    private final BidDocumentTextExtractor
            textExtractor;


    private final BidRagChunker
            chunker;


    private final OllamaRagGateway
            ollamaGateway;


    private final RagEmbeddingCodec
            embeddingCodec;


    private final CosineSimilarity
            cosineSimilarity;


    private final RagSettings
            settings;


    private final LocalAiSettings
            localAiSettings;


    public BidRagService(
            TenderMatchRepository matchRepository,
            BidDecisionRepository decisionRepository,
            BidDocumentRepository documentRepository,
            BidDocumentTextRepository textRepository,
            BidRagChunkRepository chunkRepository,
            BidRagQueryRepository queryRepository,
            BidDocumentStorageGateway storageGateway,
            BidDocumentTextExtractor textExtractor,
            BidRagChunker chunker,
            OllamaRagGateway ollamaGateway,
            RagEmbeddingCodec embeddingCodec,
            CosineSimilarity cosineSimilarity,
            RagSettings settings,
            LocalAiSettings localAiSettings
    ) {

        this.matchRepository =
                matchRepository;

        this.decisionRepository =
                decisionRepository;

        this.documentRepository =
                documentRepository;

        this.textRepository =
                textRepository;

        this.chunkRepository =
                chunkRepository;

        this.queryRepository =
                queryRepository;

        this.storageGateway =
                storageGateway;

        this.textExtractor =
                textExtractor;

        this.chunker =
                chunker;

        this.ollamaGateway =
                ollamaGateway;

        this.embeddingCodec =
                embeddingCodec;

        this.cosineSimilarity =
                cosineSimilarity;

        this.settings =
                settings;

        this.localAiSettings =
                localAiSettings;
    }


    public RagStatusResponse status() {

        OllamaRagGateway.RagModelStatus status =
                ollamaGateway
                        .status();


        return new RagStatusResponse(
                status.enabled(),

                status.reachable(),

                status.generationModelAvailable(),

                status.embeddingModelAvailable(),

                status.generationModel(),

                status.embeddingModel(),

                status.baseUrl(),

                "POSTGRESQL_PRIVATE_PER_TENDER",

                "ORGANIZATION_PRIVATE"
        );
    }


    @Transactional(readOnly = true)
    public BidRagIndexStatusResponse indexStatus(
            UUID organizationId,
            UUID tenderId
    ) {

        requireMatch(
                organizationId,
                tenderId
        );


        List<BidDocumentEntity> documents =
                documentRepository
                        .findByOrganizationIdAndTender_IdOrderByUploadedAtDesc(
                                organizationId,
                                tenderId
                        );


        List<BidRagChunkEntity> chunks =
                chunkRepository
                        .findByOrganizationIdAndTender_IdOrderByDocument_IdAscChunkIndexAsc(
                                organizationId,
                                tenderId
                        );


        long indexedDocuments =
                chunks
                        .stream()
                        .map(
                                chunk ->
                                        chunk
                                                .getDocument()
                                                .getId()
                        )
                        .distinct()
                        .count();


        return new BidRagIndexStatusResponse(
                documents.size(),

                Math.toIntExact(
                        indexedDocuments
                ),

                chunks.size(),

                !chunks.isEmpty(),

                settings.embeddingModel()
        );
    }


    @Transactional
    public BidRagRebuildResponse rebuild(
            UUID organizationId,
            UUID tenderId
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        requireModels();


        List<BidDocumentEntity> documents =
                documentRepository
                        .findByOrganizationIdAndTender_IdOrderByUploadedAtDesc(
                                organizationId,
                                tenderId
                        );


        chunkRepository
                .deleteByOrganizationIdAndTender_Id(
                        organizationId,
                        tenderId
                );


        chunkRepository.flush();


        int indexedDocuments =
                0;

        int skippedDocuments =
                0;

        int chunkCount =
                0;


        for (
                BidDocumentEntity document :
                documents
        ) {

            try {

                BidDocumentTextEntity text =
                        ensureText(
                                organizationId,
                                tenderId,
                                document
                        );


                List<BidRagChunk> chunks =
                        chunker
                                .chunk(
                                        text.getExtractedText()
                                );


                if (
                        chunks.isEmpty()
                ) {

                    skippedDocuments++;

                    continue;
                }


                List<List<Double>> embeddings =
                        embedBatches(
                                chunks
                                        .stream()
                                        .map(
                                                BidRagChunk::text
                                        )
                                        .toList()
                        );


                if (
                        embeddings.size()
                                !=
                                chunks.size()
                ) {

                    throw new IllegalStateException(
                            "Embedding result count does not match chunk count"
                    );
                }


                List<BidRagChunkEntity> entities =
                        new ArrayList<>(
                                chunks.size()
                        );


                for (
                        int index = 0;
                        index < chunks.size();
                        index++
                ) {

                    BidRagChunk chunk =
                            chunks.get(
                                    index
                            );


                    List<Double> embedding =
                            embeddings.get(
                                    index
                            );


                    entities.add(
                            BidRagChunkEntity
                                    .create(
                                            organizationId,
                                            match.getTender(),
                                            document,
                                            chunk,
                                            sha256(
                                                    chunk
                                                            .text()
                                                            .getBytes(
                                                                    StandardCharsets.UTF_8
                                                            )
                                            ),
                                            settings.embeddingModel(),
                                            embedding.size(),
                                            embeddingCodec
                                                    .encode(
                                                            embedding
                                                    )
                                    )
                    );
                }


                chunkRepository
                        .saveAll(
                                entities
                        );


                chunkRepository.flush();


                indexedDocuments++;

                chunkCount +=
                        entities.size();


            } catch (
                    InvalidRequestException exception
            ) {

                skippedDocuments++;
            }
        }


        return new BidRagRebuildResponse(
                documents.size(),
                indexedDocuments,
                skippedDocuments,
                chunkCount,
                settings.embeddingModel()
        );
    }


    @Transactional
    public BidRagAnswerResponse ask(
            UUID organizationId,
            UUID tenderId,
            AskBidDocumentsRequest request,
            String actor
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        requireModels();


        String question =
                validateQuestion(
                        request
                );


        List<BidRagChunkEntity> chunks =
                chunkRepository
                        .findByOrganizationIdAndTender_IdOrderByDocument_IdAscChunkIndexAsc(
                                organizationId,
                                tenderId
                        );


        if (
                chunks.isEmpty()
        ) {

            throw new InvalidRequestException(
                    "The private RAG index is empty. Rebuild the document index first."
            );
        }


        List<List<Double>> questionEmbeddings =
                ollamaGateway
                        .embed(
                                List.of(
                                        question
                                )
                        );


        if (
                questionEmbeddings.size()
                        !=
                        1
        ) {

            throw new IllegalStateException(
                    "Question embedding response is invalid"
            );
        }


        List<Double> questionEmbedding =
                questionEmbeddings
                        .getFirst();


        int topK =
                resolvedTopK(
                        request.topK()
                );


        List<ScoredChunk> ranked =
                chunks
                        .stream()
                        .map(
                                chunk ->
                                        new ScoredChunk(
                                                chunk,

                                                cosineSimilarity
                                                        .score(
                                                                questionEmbedding,

                                                                embeddingCodec
                                                                        .decode(
                                                                                chunk.getEmbeddingData()
                                                                        )
                                                        )
                                        )
                        )
                        .sorted(
                                Comparator
                                        .comparingDouble(
                                                ScoredChunk::similarity
                                        )
                                        .reversed()
                        )
                        .toList();


        List<ScoredChunk> selected =
                ranked
                        .stream()
                        .filter(
                                item ->
                                        item.similarity()
                                                >=
                                                settings.minimumSimilarity()
                        )
                        .limit(
                                topK
                        )
                        .toList();


        if (
                selected.isEmpty()
        ) {

            BidRagQueryEntity query =
                    BidRagQueryEntity
                            .create(
                                    organizationId,

                                    match.getTender(),

                                    question,

                                    "I could not find sufficient evidence in the indexed bid documents to answer this question.",

                                    false,

                                    localAiSettings.model(),

                                    settings.embeddingModel(),

                                    settings.promptVersion(),

                                    actor(
                                            actor
                                    ),

                                    List.of()
                            );


            queryRepository
                    .saveAndFlush(
                            query
                    );


            return response(
                    query
            );
        }


        List<RagContextItem> contexts =
                new ArrayList<>(
                        selected.size()
                );


        for (
                int index = 0;
                index < selected.size();
                index++
        ) {

            ScoredChunk scored =
                    selected.get(
                            index
                    );


            BidRagChunkEntity chunk =
                    scored.chunk();


            contexts.add(
                    new RagContextItem(
                            "S"
                                    +
                                    (index + 1),

                            chunk.getId(),

                            chunk
                                    .getDocument()
                                    .getId(),

                            chunk
                                    .getDocument()
                                    .getOriginalFilename(),

                            chunk.getChunkIndex(),

                            scored.similarity(),

                            chunk.getChunkText()
                    )
            );
        }


        OllamaRagAnswerResult generated =
                ollamaGateway
                        .answer(
                                question,
                                List.copyOf(
                                        contexts
                                )
                        );


        ValidatedAnswer answer =
                validateGeneratedAnswer(
                        generated,
                        contexts
                );


        BidRagQueryEntity query =
                BidRagQueryEntity
                        .create(
                                organizationId,

                                match.getTender(),

                                question,

                                answer.answer(),

                                answer.supported(),

                                localAiSettings.model(),

                                settings.embeddingModel(),

                                settings.promptVersion(),

                                actor(
                                        actor
                                ),

                                answer.citations()
                        );


        queryRepository
                .saveAndFlush(
                        query
                );


        return response(
                query
        );
    }


    @Transactional(readOnly = true)
    public List<BidRagAnswerResponse> history(
            UUID organizationId,
            UUID tenderId
    ) {

        requireMatch(
                organizationId,
                tenderId
        );


        return queryRepository
                .findTop25ByOrganizationIdAndTender_IdOrderByCreatedAtDesc(
                        organizationId,
                        tenderId
                )
                .stream()
                .map(
                        this::response
                )
                .toList();
    }


    private BidDocumentTextEntity ensureText(
            UUID organizationId,
            UUID tenderId,
            BidDocumentEntity document
    ) {

        BidDocumentTextEntity existing =
                textRepository
                        .findByOrganizationIdAndTender_IdAndDocument_Id(
                                organizationId,
                                tenderId,
                                document.getId()
                        )
                        .orElse(
                                null
                        );


        if (
                existing != null
                &&
                existing.isCurrentFor(
                        document.getSha256()
                )
        ) {

            return existing;
        }


        byte[] content =
                storageGateway
                        .load(
                                document.getStorageKey()
                        );


        BidDocumentExtractionResult result =
                textExtractor
                        .extract(
                                document.getOriginalFilename(),
                                content
                        );


        String textSha =
                sha256(
                        result
                                .text()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        BidDocumentTextEntity text =
                existing == null
                        ? BidDocumentTextEntity
                                .create(
                                        organizationId,
                                        document.getTender(),
                                        document
                                )
                        : existing;


        text.apply(
                document.getSha256(),
                textSha,
                result
        );


        return textRepository
                .saveAndFlush(
                        text
                );
    }


    private List<List<Double>> embedBatches(
            List<String> values
    ) {

        List<List<Double>> result =
                new ArrayList<>(
                        values.size()
                );


        for (
                int start = 0;
                start < values.size();
                start += EMBEDDING_BATCH_SIZE
        ) {

            int end =
                    Math.min(
                            values.size(),
                            start
                                    +
                                    EMBEDDING_BATCH_SIZE
                    );


            List<String> batch =
                    values.subList(
                            start,
                            end
                    );


            result.addAll(
                    ollamaGateway
                            .embed(
                                    List.copyOf(
                                            batch
                                    )
                            )
            );
        }


        return List.copyOf(
                result
        );
    }


    private ValidatedAnswer validateGeneratedAnswer(
            OllamaRagAnswerResult generated,
            List<RagContextItem> contexts
    ) {

        if (
                generated == null
                ||
                generated.answer() == null
                ||
                generated.answer().isBlank()
                ||
                generated.supported() == null
                ||
                generated.sourceIds() == null
        ) {

            throw new IllegalStateException(
                    "Local RAG model returned an invalid structured answer"
            );
        }


        Map<String, RagContextItem> bySource =
                new HashMap<>();


        for (
                RagContextItem context :
                contexts
        ) {

            bySource.put(
                    context.sourceId(),
                    context
            );
        }


        Set<String> requested =
                new LinkedHashSet<>(
                        generated.sourceIds()
                );


        List<BidRagCitationValue> citations =
                new ArrayList<>();


        for (
                String sourceId :
                requested
        ) {

            RagContextItem context =
                    bySource.get(
                            sourceId
                    );


            if (
                    context == null
            ) {

                throw new IllegalStateException(
                        "Local RAG model cited an unknown source ID"
                );
            }


            citations.add(
                    new BidRagCitationValue(
                            context.chunkId(),
                            context.sourceId(),
                            context.documentId(),
                            context.filename(),
                            context.chunkIndex(),
                            context.similarity(),
                            excerpt(
                                    context.text()
                            )
                    )
            );
        }


        if (
                generated.supported()
                &&
                citations.isEmpty()
        ) {

            throw new IllegalStateException(
                    "A supported RAG answer must contain at least one valid citation"
            );
        }


        return new ValidatedAnswer(
                generated.answer().trim(),

                generated.supported(),

                List.copyOf(
                        citations
                )
        );
    }


    private BidRagAnswerResponse response(
            BidRagQueryEntity query
    ) {

        return new BidRagAnswerResponse(
                query.getId(),

                query.getQuestion(),

                query.getAnswer(),

                query.isSupported(),

                query.getGenerationModel(),

                query.getEmbeddingModel(),

                query.getCreatedAt(),

                query
                        .getCitations()
                        .stream()
                        .map(
                                citation ->
                                        new BidRagAnswerResponse
                                                .CitationResponse(
                                                        citation.getSourceId(),
                                                        citation.getDocumentId(),
                                                        citation.getFilename(),
                                                        citation.getChunkIndex(),
                                                        citation.getSimilarity(),
                                                        citation.getExcerpt()
                                                )
                        )
                        .toList()
        );
    }


    private TenderMatchEntity requireMatch(
            UUID organizationId,
            UUID tenderId
    ) {

        return matchRepository
                .findByOrganizationIdAndTender_Id(
                        organizationId,
                        tenderId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "No company-to-tender match exists for this organization and opportunity"
                                )
                );
    }


    private TenderMatchEntity requireWritableMatch(
            UUID organizationId,
            UUID tenderId
    ) {

        TenderMatchEntity match =
                requireMatch(
                        organizationId,
                        tenderId
                );


        BidDecisionEntity decision =
                decisionRepository
                        .findByOrganizationIdAndTender_Id(
                                organizationId,
                                tenderId
                        )
                        .orElse(
                                null
                        );


        if (
                decision != null
                &&
                decision.getOutcome()
                        ==
                        BidDecisionOutcome.NO_GO
        ) {

            throw new InvalidRequestException(
                    "Private RAG is locked because the current bid decision is NO-GO"
            );
        }


        return match;
    }


    private void requireModels() {

        OllamaRagGateway.RagModelStatus status =
                ollamaGateway
                        .status();


        if (
                !status.enabled()
        ) {

            throw new InvalidRequestException(
                    "Private RAG is disabled"
            );
        }


        if (
                !status.reachable()
        ) {

            throw new InvalidRequestException(
                    "Local Ollama is not reachable"
            );
        }


        if (
                !status.generationModelAvailable()
        ) {

            throw new InvalidRequestException(
                    "The configured Ollama generation model is not installed"
            );
        }


        if (
                !status.embeddingModelAvailable()
        ) {

            throw new InvalidRequestException(
                    "The configured Ollama embedding model is not installed"
            );
        }
    }


    private String validateQuestion(
            AskBidDocumentsRequest request
    ) {

        if (
                request == null
                ||
                request.question() == null
        ) {

            throw new InvalidRequestException(
                    "Question is required"
            );
        }


        String question =
                request
                        .question()
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        if (
                question.length()
                        <
                        3
        ) {

            throw new InvalidRequestException(
                    "Question is too short"
            );
        }


        if (
                question.length()
                        >
                        settings.maximumQuestionChars()
        ) {

            throw new InvalidRequestException(
                    "Question exceeds the configured maximum length"
            );
        }


        return question;
    }


    private int resolvedTopK(
            Integer value
    ) {

        if (
                value == null
        ) {

            return settings.defaultTopK();
        }


        return Math.max(
                1,
                Math.min(
                        settings.maxTopK(),
                        value
                )
        );
    }


    private String excerpt(
            String value
    ) {

        String normalized =
                value
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();


        if (
                normalized.length()
                        <=
                        700
        ) {

            return normalized;
        }


        return normalized.substring(
                0,
                700
        )
                +
                "…";
    }


    private String actor(
            String value
    ) {

        if (
                value == null
                ||
                value.isBlank()
        ) {

            return "unknown";
        }


        return value.trim();
    }


    private String sha256(
            byte[] content
    ) {

        try {

            return HexFormat
                    .of()
                    .formatHex(
                            MessageDigest
                                    .getInstance(
                                            "SHA-256"
                                    )
                                    .digest(
                                            content
                                    )
                    );


        } catch (
                NoSuchAlgorithmException exception
        ) {

            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }


    private record ScoredChunk(

            BidRagChunkEntity chunk,

            double similarity

    ) {
    }


    private record ValidatedAnswer(

            String answer,

            boolean supported,

            List<BidRagCitationValue> citations

    ) {
    }

}
