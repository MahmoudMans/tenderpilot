export type RagStatus = {

  enabled:
    boolean;

  reachable:
    boolean;

  generationModelAvailable:
    boolean;

  embeddingModelAvailable:
    boolean;

  generationModel:
    string;

  embeddingModel:
    string;

  baseUrl:
    string;

  vectorStore:
    string;

  dataBoundary:
    string;

};


export type BidRagIndexStatus = {

  documentCount:
    number;

  indexedDocumentCount:
    number;

  chunkCount:
    number;

  ready:
    boolean;

  embeddingModel:
    string;

};


export type BidRagRebuildResult = {

  totalDocuments:
    number;

  indexedDocuments:
    number;

  skippedDocuments:
    number;

  chunks:
    number;

  embeddingModel:
    string;

};


export type BidRagAnswer = {

  id:
    string;

  question:
    string;

  answer:
    string;

  supported:
    boolean;

  generationModel:
    string;

  embeddingModel:
    string;

  createdAt:
    string;

  citations: Array<{

    sourceId:
      string;

    documentId:
      string;

    filename:
      string;

    chunkIndex:
      number;

    similarity:
      number;

    excerpt:
      string;

  }>;

};
