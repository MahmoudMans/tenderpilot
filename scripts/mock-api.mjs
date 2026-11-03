// TenderPilot mock API — SYNTHETIC DEMO DATA ONLY. Pure functions, no dependencies.
//
// `handle(method, pathname, search, body)` answers the subset of /api/v1 the
// frontend reads, with deterministic-looking fake tenders, buyers and suppliers.
// It is served over HTTP by scripts/mock-api-server.mjs so the whole UI can be
// explored without Postgres, OpenSearch or Ollama:
//
//     ./scripts/run-frontend-demo.sh
//
// Set MOCK_FRESH=1 to simulate a brand-new account (empty dashboard, onboarding
// checklist visible). Set MOCK_MORE=1 so Search reports more hits than it returns
// (to try the "Show more results" button). Set MOCK_STALE=1 so the company profile
// looks edited after the last match calculation (Matches shows its recalculate
// banner), or MOCK_NOPROFILE=1 for an account without a company profile.
// This file is a developer convenience;
// it is not used by the product and never talks to the real backend.

const DAY = 86400000;

// When the matches were last calculated. Recalculating moves it to now.
let matchesEvaluatedAt = null;
const iso = (offsetDays, hour = 9) => {
  const d = new Date();
  d.setUTCHours(hour, 0, 0, 0);
  return new Date(d.getTime() + offsetDays * DAY).toISOString();
};
const ymd = (offsetDays) => iso(offsetDays).slice(0, 10);

export const user = {
  id: '11111111-1111-4111-8111-111111111111',
  fullName: 'Amira Ben Salah',
  email: 'amira@atlasbuild.example',
  organization: { id: '22222222-2222-4222-8222-222222222222', name: 'Atlas Build & Energy', slug: 'atlas-build-energy' },
  role: 'OWNER',
};

const CPV = {
  energy: { code: '45310000', label: 'Electrical installation work' },
  hvac: { code: '45331000', label: 'Heating, ventilation and air-conditioning installation work' },
  insul: { code: '45321000', label: 'Thermal insulation work' },
  renov: { code: '45454000', label: 'Restructuring work' },
  access: { code: '45262700', label: 'Building alteration work' },
  solar: { code: '09331200', label: 'Solar photovoltaic modules' },
  maint: { code: '50700000', label: 'Repair and maintenance services of building installations' },
  design: { code: '71320000', label: 'Engineering design services' },
};

const BUYERS = [
  { id: 'b0000001-0000-4000-8000-000000000001', name: 'Métropole Val-de-Rive', category: 'LOCAL_AUTHORITY', city: 'Lyon', region: 'Auvergne-Rhône-Alpes' },
  { id: 'b0000001-0000-4000-8000-000000000002', name: 'Ville de Montfleur', category: 'LOCAL_AUTHORITY', city: 'Montfleur', region: 'Occitanie' },
  { id: 'b0000001-0000-4000-8000-000000000003', name: 'Agglomération des Rives', category: 'LOCAL_AUTHORITY', city: 'Annecy', region: 'Auvergne-Rhône-Alpes' },
  { id: 'b0000001-0000-4000-8000-000000000004', name: 'Département du Haut-Vallon', category: 'CENTRAL_GOVERNMENT', city: 'Grenoble', region: 'Auvergne-Rhône-Alpes' },
  { id: 'b0000001-0000-4000-8000-000000000005', name: 'CHU Saint-Ambroise', category: 'HEALTHCARE', city: 'Toulouse', region: 'Occitanie' },
  { id: 'b0000001-0000-4000-8000-000000000006', name: 'Lycée Jean-Moulin (Région Sud)', category: 'EDUCATION', city: 'Marseille', region: "Provence-Alpes-Côte d'Azur" },
  { id: 'b0000001-0000-4000-8000-000000000007', name: 'Habitat Rhône Alpes', category: 'SOCIAL_HOUSING', city: 'Villeurbanne', region: 'Auvergne-Rhône-Alpes' },
  { id: 'b0000001-0000-4000-8000-000000000008', name: 'Syndicat des Eaux du Plateau', category: 'UTILITIES', city: 'Chambéry', region: 'Auvergne-Rhône-Alpes' },
];

const T = (n, title, buyerIx, over) => ({
  id: `a${String(n).padStart(7, '0')}-0000-4000-8000-00000000000${n % 10}`,
  title,
  buyerIx,
  ...over,
});

const RAW = [
  T(1, 'Energy retrofit of the municipal administration complex', 0, { status: 'PUBLISHED', procedure: 'OPEN', nature: 'WORKS', pub: -9, dl: 2, min: 1800000, max: 2400000, cpvs: [CPV.energy, CPV.insul], sources: ['BOAMP', 'TED'], lots: 3, q: 92, score: 94, grade: 'EXCELLENT', decision: 'GO' }),
  T(2, 'HVAC and thermal insulation renovation — school complex', 1, { status: 'PUBLISHED', procedure: 'OPEN', nature: 'WORKS', pub: -12, dl: 5, min: 900000, max: 1250000, cpvs: [CPV.hvac, CPV.insul], sources: ['BOAMP'], lots: 2, q: 88, score: 88, grade: 'EXCELLENT', decision: 'GO' }),
  T(3, 'Historic library envelope restoration', 2, { status: 'PUBLISHED', procedure: 'RESTRICTED', nature: 'WORKS', pub: -20, dl: 18, min: 2100000, max: 2900000, cpvs: [CPV.renov], sources: ['TED'], lots: 4, q: 81, score: 82, grade: 'STRONG', decision: 'HOLD' }),
  T(4, 'Accessibility upgrade of the departmental administrative centre', 3, { status: 'PUBLISHED', procedure: 'OPEN', nature: 'WORKS', pub: -6, dl: 12, min: 650000, max: 820000, cpvs: [CPV.access], sources: ['BOAMP'], lots: 1, q: 74, score: 79, grade: 'STRONG', decision: 'PENDING' }),
  T(5, 'Rooftop photovoltaic installation — university hospital', 4, { status: 'PUBLISHED', procedure: 'COMPETITIVE_WITH_NEGOTIATION', nature: 'WORKS', pub: -4, dl: 26, min: 3200000, max: 4100000, cpvs: [CPV.solar, CPV.energy], sources: ['TED'], lots: 2, q: 90, score: 76, grade: 'STRONG', decision: null }),
  T(6, 'Maintenance of building technical installations (framework, 4 years)', 5, { status: 'PUBLISHED', procedure: 'OPEN', nature: 'SERVICES', pub: -15, dl: 9, min: 1200000, max: 1200000, cpvs: [CPV.maint], sources: ['BOAMP'], lots: 5, q: 69, score: 71, grade: 'GOOD', decision: 'PENDING' }),
  T(7, 'Social housing — deep thermal renovation, 214 units', 6, { status: 'PUBLISHED', procedure: 'OPEN', nature: 'WORKS', pub: -3, dl: 34, min: 5400000, max: 6300000, cpvs: [CPV.insul, CPV.hvac], sources: ['BOAMP', 'TED'], lots: 6, q: 85, score: 68, grade: 'GOOD', decision: null }),
  T(8, 'Engineering design services — water treatment plant upgrade', 7, { status: 'PUBLISHED', procedure: 'OPEN', nature: 'SERVICES', pub: -8, dl: 15, min: 380000, max: 520000, cpvs: [CPV.design], sources: ['TED'], lots: 1, q: 63, score: 58, grade: 'GOOD', decision: 'NO_GO' }),
  T(9, 'Replacement of boilers in municipal sports facilities', 1, { status: 'PUBLISHED', procedure: 'OPEN', nature: 'WORKS', pub: -2, dl: 21, min: 420000, max: 510000, cpvs: [CPV.hvac], sources: ['BOAMP'], lots: 2, q: 77, score: 63, grade: 'GOOD', decision: null }),
  T(10, 'Smart metering deployment for district heating', 0, { status: 'UPDATED', procedure: 'COMPETITIVE_DIALOGUE', nature: 'MIXED', pub: -26, dl: 6, min: 2600000, max: 3400000, cpvs: [CPV.energy, CPV.design], sources: ['TED'], lots: 3, q: 58, score: 41, grade: 'WEAK', decision: null }),
  T(11, 'Cleaning services for administrative buildings', 3, { status: 'PUBLISHED', procedure: 'OPEN', nature: 'SERVICES', pub: -7, dl: 11, min: 300000, max: 360000, cpvs: [{ code: '90910000', label: 'Cleaning services' }], sources: ['BOAMP'], lots: 1, q: 52, score: 12, grade: 'BLOCKED', blocked: true, decision: null }),
  T(12, 'Extension of the vocational training centre', 5, { status: 'CLOSED', procedure: 'OPEN', nature: 'WORKS', pub: -45, dl: -6, min: 1500000, max: 1900000, cpvs: [CPV.renov], sources: ['BOAMP'], lots: 2, q: 80, score: 73, grade: 'GOOD', decision: null }),
];

function summaryOf(r) {
  const b = BUYERS[r.buyerIx];
  return {
    id: r.id,
    title: r.title,
    buyerName: b.name,
    status: r.status,
    procedureType: r.procedure,
    contractNature: r.nature,
    publicationDate: ymd(r.pub),
    submissionDeadline: iso(r.dl, 12),
    estimatedMinValue: r.min,
    estimatedMaxValue: r.max,
    currency: 'EUR',
    countryCode: 'FR',
    primaryRegionName: b.region,
    cpvs: r.cpvs,
    sources: r.sources,
    lotCount: r.lots,
    awardedAmount: r.status === 'CLOSED' ? Math.round(r.min * 0.93) : null,
    awardedCurrency: r.status === 'CLOSED' ? 'EUR' : null,
    dataQualityScore: r.q,
    dataQualityGrade: r.q >= 85 ? 'EXCELLENT' : r.q >= 70 ? 'GOOD' : r.q >= 55 ? 'FAIR' : 'POOR',
    dataQualityIssueCount: r.q >= 85 ? 0 : r.q >= 70 ? 2 : 5,
  };
}

function detailOf(r) {
  const b = BUYERS[r.buyerIx];
  const s = summaryOf(r);
  return {
    id: r.id,
    canonicalKey: `fr-${r.sources[0].toLowerCase()}-${r.id.slice(0, 8)}`,
    title: r.title,
    description:
      'The contracting authority is seeking a qualified contractor to deliver the works described in the tender documents, including design coordination, site preparation, execution, commissioning and handover. Tenderers must demonstrate relevant references and valid certifications.',
    status: r.status,
    procedureType: r.procedure,
    contractNature: r.nature,
    publicationDate: s.publicationDate,
    submissionDeadline: s.submissionDeadline,
    estimatedMinValue: r.min,
    estimatedMaxValue: r.max,
    currency: 'EUR',
    countryCode: 'FR',
    primaryRegionCode: 'FR-ARA',
    primaryRegionName: b.region,
    frameworkAgreement: r.nature === 'SERVICES',
    jointProcurement: false,
    electronicSubmission: true,
    buyer: { id: b.id, legalName: b.name, category: b.category, countryCode: 'FR', city: b.city, registrationNumber: '21690123400017', websiteUrl: 'https://example.org' },
    cpvs: r.cpvs,
    locations: [{ countryCode: 'FR', regionCode: 'FR-ARA', regionName: b.region, city: b.city }],
    lots: Array.from({ length: r.lots }, (_, i) => ({
      id: `lot-${r.id.slice(0, 4)}-${i + 1}`,
      lotNumber: String(i + 1),
      title: `Lot ${i + 1} — ${['General works', 'Technical installations', 'Finishing and insulation', 'External works', 'Commissioning', 'Maintenance'][i % 6]}`,
      description: 'Scope as defined in the technical specifications (CCTP).',
      estimatedMinValue: Math.round(r.min / r.lots),
      estimatedMaxValue: Math.round(r.max / r.lots),
      currency: 'EUR',
      cpvs: r.cpvs.slice(0, 1),
    })),
    sources: r.sources.map((src, i) => ({
      source: src,
      externalId: `${src}-2026-${100000 + r.pub * -7 + i}`,
      externalVersion: '1',
      noticeType: 'Contract notice',
      sourceUrl: 'https://example.org/notice',
      payloadChecksum: 'a3f5c9d1e2b4',
      sourcePublishedAt: iso(r.pub),
      firstSeenAt: iso(r.pub, 10),
      lastSeenAt: iso(-0.2, 7),
    })),
    quality: {
      score: r.q,
      grade: s.dataQualityGrade,
      criticalIssueCount: 0,
      warningIssueCount: r.q >= 85 ? 0 : 2,
      infoIssueCount: 1,
      assessedAt: iso(-1),
      issues: r.q >= 85 ? [{ code: 'MISSING_LOT_DESCRIPTION', severity: 'INFO', fieldName: 'lots.description', message: 'One lot has a short description.' }] : [
        { code: 'MISSING_REGION_CODE', severity: 'WARNING', fieldName: 'region', message: 'Region code is missing for one location.' },
        { code: 'ESTIMATED_VALUE_RANGE', severity: 'WARNING', fieldName: 'estimatedValue', message: 'Estimated value is a wide range.' },
        { code: 'MISSING_LOT_DESCRIPTION', severity: 'INFO', fieldName: 'lots.description', message: 'One lot has a short description.' },
      ],
    },
    award: r.status === 'CLOSED' ? {
      notificationDate: ymd(-3),
      awardedAmount: Math.round(r.min * 0.93),
      currency: 'EUR',
      durationMonths: 18,
      priceForm: 'Lump sum',
      offersReceived: 5,
      frameworkAgreementId: null,
      socialConsiderations: 'Local employment clause',
      environmentalConsiderations: 'Low-carbon materials',
      executionModalities: null,
      operatorGroupType: 'Single operator',
      subcontractingDeclared: 'Partial',
      suppliers: [{ identifier: '48800123400021', identifierType: 'SIRET', legalName: 'Constructions Lemoine SAS' }],
    } : null,
  };
}

const SIGNALS = (r) => [
  { code: 'CPV_MATCH', impact: 'POSITIVE', dimension: 'CPV', earnedScore: 24, maximumScore: 25, message: 'Primary CPV matches 2 of your target CPV codes.' },
  { code: 'REGION_MATCH', impact: 'POSITIVE', dimension: 'GEOGRAPHY', earnedScore: 14, maximumScore: 15, message: 'Located inside one of your operating regions.' },
  { code: 'VALUE_FIT', impact: r.score > 70 ? 'POSITIVE' : 'NEUTRAL', dimension: 'VALUE', earnedScore: r.score > 70 ? 14 : 9, maximumScore: 15, message: 'Estimated value is within your preferred contract range.' },
  { code: 'BUYER_PREF', impact: 'NEUTRAL', dimension: 'BUYER', earnedScore: 7, maximumScore: 10, message: 'Buyer type is in your preferred list.' },
  { code: 'TIMING', impact: r.dl < 7 ? 'NEGATIVE' : 'POSITIVE', dimension: 'TIMING', earnedScore: r.dl < 7 ? 3 : 9, maximumScore: 10, message: r.dl < 7 ? 'Deadline is shorter than your minimum notice period.' : 'Enough time before the deadline.' },
  ...(r.blocked ? [{ code: 'EXCLUDED_KEYWORD', impact: 'BLOCKER', dimension: 'CAPABILITY', earnedScore: 0, maximumScore: 15, message: 'Title contains an excluded keyword: "cleaning".' }] : []),
];

function matchOf(r) {
  return {
    id: 'm' + r.id.slice(1),
    score: r.score,
    grade: r.grade,
    blocked: !!r.blocked,
    cpvScore: Math.min(100, r.score + 3),
    geographyScore: Math.min(100, r.score + 6),
    valueScore: Math.max(0, r.score - 4),
    buyerScore: Math.max(0, r.score - 10),
    capabilityScore: r.score,
    timingScore: r.dl < 7 ? 40 : 85,
    qualityScore: r.q,
    evaluatedAt: iso(-0.1),
    opportunity: summaryOf(r),
    signals: SIGNALS(r),
  };
}

const matches = RAW.map(matchOf).sort((a, b) => b.score - a.score);
const decisions = RAW.filter((r) => r.decision);

function queueItem(r) {
  return {
    matchId: 'm' + r.id.slice(1),
    tenderId: r.id,
    matchScore: r.score,
    matchGrade: r.grade,
    blocked: !!r.blocked,
    opportunity: summaryOf(r),
    decision: r.decision
      ? { id: 'd' + r.id.slice(1), outcome: r.decision, recommendedOutcome: r.score >= 75 ? 'GO' : r.score >= 55 ? 'HOLD' : 'NO_GO', decisionScore: Math.round(r.score * 0.92), updatedAt: iso(-1) }
      : null,
  };
}

function dashboard() {
  const active = RAW.filter((r) => r.decision && r.decision !== 'NO_GO');
  return {
    generatedAt: new Date().toISOString(),
    summary: {
      matchedOpportunities: 42, highPotentialMatches: 11, blockedMatches: 3,
      goBids: 2, holdBids: 1, noGoBids: 1, pendingDecisionBids: 2,
      complianceReadyBids: 1, submissionReadyBids: 1, submittedBids: 0,
      openTasks: 14, overdueTasks: 2, blockedTasks: 1, criticalOpenTasks: 2,
      deadlinesNext7Days: 3, deadlinesNext30Days: 9,
    },
    market: {
      available: true, noticeCount30Days: 1284, activeNoticeCount30Days: 903, uniqueBuyerCount30Days: 412,
      uniqueObservedSupplierCount30Days: 268, buyerConcentration: 'DIVERSIFIED', supplierConcentration: 'MODERATE',
      leadingCpv: '45000000 — Construction work', leadingCpvSharePercentage: 18.4, awardCoveragePercentage30Days: 61.2,
    },
    priorities: [
      { priority: 'CRITICAL', type: 'DEADLINE', title: 'Energy retrofit of the municipal administration complex', detail: 'Submission deadline in 2 days — 1 mandatory requirement still open.', tenderId: RAW[0].id, href: `/bid-workspaces/${RAW[0].id}/planning` },
      { priority: 'HIGH', type: 'TASK', title: '2 overdue bid tasks', detail: 'Technical memo and insurance certificate are past due.', tenderId: RAW[1].id, href: `/bid-workspaces/${RAW[1].id}/planning` },
      { priority: 'HIGH', type: 'DECISION', title: 'Accessibility upgrade needs a bid decision', detail: 'Match score 79 — decision still PENDING with 12 days left.', tenderId: RAW[3].id, href: `/bid-decisions/${RAW[3].id}` },
      { priority: 'MEDIUM', type: 'MATCH', title: '3 new high-potential matches', detail: 'Rooftop PV and social housing renovation score above 70.', tenderId: null, href: '/matches' },
      { priority: 'LOW', type: 'DATA', title: 'DECP source is stale', detail: 'Last successful sync was 3 days ago.', tenderId: null, href: '/data-operations' },
    ],
    portfolio: active.concat(RAW.filter((r) => r.decision === 'PENDING' && !active.includes(r))).slice(0, 5).map((r, i) => ({
      tenderId: r.id, buyerId: BUYERS[r.buyerIx].id, title: r.title, buyerName: BUYERS[r.buyerIx].name,
      matchScore: r.score, matchGrade: r.grade, matchBlocked: false, decisionOutcome: r.decision,
      submissionDeadline: iso(r.dl, 12), daysToDeadline: r.dl,
      complianceReady: i === 1, complianceReadinessScore: [92, 100, 74, 41, 58][i] ?? 50,
      openTasks: [3, 0, 4, 2, 5][i] ?? 1, completedTasks: [9, 12, 5, 1, 2][i] ?? 0,
      overdueTasks: [1, 0, 0, 1, 0][i] ?? 0, blockedTasks: [0, 0, 1, 0, 0][i] ?? 0, criticalOpenTasks: [1, 0, 0, 0, 1][i] ?? 0,
      readyForSubmission: i === 1, submitted: false,
    })),
  };
}

// The watchlist is kept in memory so the Watch buttons behave like the real API
// (create is idempotent, delete removes) for as long as the mock server runs.
let watchlistItems = null;
function watchlist() {
  if (watchlistItems === null) {
    watchlistItems = [
      { id: 'w0000001-0000-4000-8000-000000000000', type: 'BUYER', targetKey: BUYERS[0].id, label: BUYERS[0].name, createdAt: iso(-14) },
      { id: 'w0000002-0000-4000-8000-000000000000', type: 'CPV', targetKey: '45331000', label: 'HVAC installation work', createdAt: iso(-10) },
      { id: 'w0000003-0000-4000-8000-000000000000', type: 'TENDER', targetKey: RAW[4].id, label: RAW[4].title, createdAt: iso(-3) },
    ];
  }
  return watchlistItems;
}

// Alert changes are kept while the demo server runs. Like the real API, a dismissed alert becomes
// active again (but stays dismissed) the next time alerts are refreshed.
const notificationState = new Map();

function notifications() {
  const list = [
    { type: 'DEADLINE_APPROACHING', severity: 'CRITICAL', title: 'Submission deadline in 2 days', detail: 'Energy retrofit of the municipal administration complex closes on ' + ymd(2) + '.', tenderId: RAW[0].id, href: `/bid-workspaces/${RAW[0].id}/planning`, status: 'UNREAD', h: -2 },
    { type: 'TASK_OVERDUE', severity: 'HIGH', title: '2 bid tasks are overdue', detail: 'HVAC and thermal insulation renovation — technical memo, insurance certificate.', tenderId: RAW[1].id, href: `/bid-workspaces/${RAW[1].id}/planning`, status: 'UNREAD', h: -5 },
    { type: 'WATCHLIST_BUYER', severity: 'MEDIUM', title: 'New notice from a watched buyer', detail: 'Métropole Val-de-Rive published “Smart metering deployment for district heating”.', tenderId: RAW[9].id, href: `/opportunities/${RAW[9].id}`, status: 'UNREAD', h: -20 },
    { type: 'WATCHLIST_CPV', severity: 'MEDIUM', title: '3 new notices in CPV 45331000', detail: 'HVAC installation work — new notices since your last visit.', tenderId: null, href: '/opportunities', status: 'READ', h: -30 },
    { type: 'MATCH_HIGH', severity: 'LOW', title: 'New strong match', detail: 'Rooftop photovoltaic installation — university hospital scores 76.', tenderId: RAW[4].id, href: `/opportunities/${RAW[4].id}`, status: 'READ', h: -52 },
  ];
  const items = list.map((n, i) => {
    const id = `n000000${i}-0000-4000-8000-000000000000`;
    return { n, id, ...(notificationState.get(id) ?? { status: n.status, active: true }) };
  });
  const active = items.filter((x) => x.active);
  return {
    summary: {
      activeCount: active.length,
      unreadCount: active.filter((x) => x.status === 'UNREAD').length,
      criticalCount: active.filter((x) => x.n.severity === 'CRITICAL').length,
      highCount: active.filter((x) => x.n.severity === 'HIGH').length,
      watchlistCount: watchlist().length,
    },
    notifications: items.map(({ n, id, status, active }) => ({ id, type: n.type, severity: n.severity, title: n.title, detail: n.detail,
      tenderId: n.tenderId, href: n.href, status, active,
      createdAt: iso(n.h / 24, 8), updatedAt: iso(n.h / 24, 8), lastObservedAt: iso(n.h / 24, 8),
    })),
    watchlist: watchlist().map((item) => ({ ...item })),
  };
}

function company() {
  return {
    exists: !process.env.MOCK_NOPROFILE, organizationId: user.organization.id, profileId: 'p0000000-0000-4000-8000-000000000000',
    legalName: 'Atlas Build & Energy SAS', tradingName: 'Atlas Build', industry: 'Construction & energy efficiency',
    description: 'Mid-sized contractor specialised in thermal renovation, HVAC and building-integrated renewable energy for public clients.',
    websiteUrl: 'https://atlasbuild.example', countryCode: 'FR', headquartersCity: 'Lyon',
    employeeBand: 'FIFTY_TO_TWO_FORTY_NINE', annualRevenueBand: 'FROM_10M_TO_50M',
    preferredMinContractValue: 400000, preferredMaxContractValue: 6000000, preferredCurrency: 'EUR',
    yearsExperience: 17, minimumNoticeDays: 10,
    capabilities: ['Thermal renovation', 'HVAC installation', 'Photovoltaic integration', 'Building maintenance', 'Design-build'],
    operatingRegions: [{ countryCode: 'FR', regionCode: 'FR-ARA', regionName: 'Auvergne-Rhône-Alpes' }, { countryCode: 'FR', regionCode: 'FR-OCC', regionName: 'Occitanie' }],
    targetCpvs: [CPV.energy, CPV.hvac, CPV.insul, CPV.solar],
    certifications: [{ name: 'ISO 9001', issuer: 'AFNOR', certificateNumber: 'QUAL-88213', validFrom: '2024-02-01', expiresOn: '2027-01-31', validityStatus: 'VALID' }, { name: 'Qualibat 5112', issuer: 'Qualibat', certificateNumber: 'Q-5112-771', validFrom: '2023-06-01', expiresOn: ymd(40), validityStatus: 'EXPIRING_SOON' }],
    referenceProjects: [{ projectName: 'Retrofit of Lycée Carnot', clientName: 'Région Auvergne-Rhône-Alpes', countryCode: 'FR', regionName: 'Auvergne-Rhône-Alpes', contractValue: 2300000, currency: 'EUR', startedOn: '2022-03-01', completedOn: '2023-09-15', description: 'Full envelope and HVAC retrofit of a 9,000 m² school.' }],
    preferredBuyerTypes: ['LOCAL_AUTHORITY', 'EDUCATION', 'SOCIAL_HOUSING'],
    excludedKeywords: ['cleaning', 'catering'],
    completeness: { score: 86, sections: [
      { key: 'identity', label: 'Company identity', points: 20, maximumPoints: 20 },
      { key: 'capabilities', label: 'Capabilities', points: 18, maximumPoints: 20 },
      { key: 'targeting', label: 'Targeting', points: 20, maximumPoints: 20 },
      { key: 'certifications', label: 'Certifications', points: 14, maximumPoints: 20 },
      { key: 'references', label: 'Reference projects', points: 14, maximumPoints: 20 },
    ] },
    createdAt: iso(-60), updatedAt: process.env.MOCK_STALE ? iso(-1) : iso(-4),
  };
}

function buyerSummary(b, i) {
  return {
    buyerId: b.id, name: b.name, category: b.category, countryCode: 'FR', city: b.city,
    noticeCount: 80 - i * 7, activeNoticeCount: 22 - i * 2, awardCount: 41 - i * 3,
    totalAwardedValue: 18400000 - i * 1300000, awardCurrency: 'EUR', averageEstimatedValue: 1100000 - i * 60000, estimatedCurrency: 'EUR',
    lastPublicationDate: ymd(-2 - i), recurringProcurement: i % 2 === 0, topCpvSharePercentage: 32 - i * 2,
    supplierConcentration: ['DIVERSIFIED', 'MODERATE', 'CONCENTRATED'][i % 3], topSupplierSharePercentage: 14 + i * 3, supplierHhi: 900 + i * 400,
    medianResponseDays: 28 - i, deadlineTempo: ['STANDARD', 'SHORT', 'LONG'][i % 3],
  };
}

const SUPPLIERS = ['Constructions Lemoine SAS', 'Thermique Alpes SA', 'Groupe Verdier Bâtiment', 'Énergies du Sud SARL', 'Bâtir & Durable', 'Ingénierie Rhône-Conseil'];
function supplierSummary(name, i) {
  return {
    supplierKey: 'sup-' + name.toLowerCase().replace(/[^a-z]+/g, '-'), name,
    awardCount: 34 - i * 4, uniqueBuyerCount: 12 - i, uniqueCountryCount: 1 + (i % 2),
    associatedAwardValue: 22000000 - i * 2100000, averageAssociatedAwardValue: 780000 - i * 40000, awardCurrency: 'EUR',
    lastObservedPublicationDate: ymd(-3 - i), topBuyerSharePercentage: 22 + i * 4, buyerHhi: 1100 + i * 300,
    buyerConcentration: ['DIVERSIFIED', 'MODERATE', 'CONCENTRATED'][i % 3], repeatBuyerSignal: i % 2 === 0,
    topCpvSharePercentage: 40 - i * 3, cpvSpecialization: ['FOCUSED', 'BROAD', 'SPECIALIZED'][i % 3],
  };
}

function market() {
  const months = ['2026-05', '2026-06', '2026-07', '2026-08', '2026-09', '2026-10'];
  return {
    filters: { query: null, country: null, region: null, cpvPrefix: null, status: null, dateFrom: null, dateTo: null },
    capabilities: { countryAvailable: true, regionAvailable: true, cpvAvailable: true, awardsAvailable: true, suppliersAvailable: true, procedureAvailable: true, contractNatureAvailable: true, publicationDateAvailable: true, deadlineAvailable: true },
    summary: {
      noticeCount: 4120, activeNoticeCount: 1630, awardedNoticeCount: 1214, uniqueBuyerCount: 902, uniqueObservedSupplierCount: 733,
      cpvCoveragePercentage: 96.2, geographyCoveragePercentage: 88.4, awardCoveragePercentage: 61.2, supplierCoveragePercentage: 54.9,
      medianResponseDays: 29, averageEstimatedValue: 1340000, estimatedCurrency: 'EUR', totalAwardedValue: 981000000, awardedCurrency: 'EUR',
      topBuyerSharePercentage: 4.1, buyerHhi: 212, buyerConcentration: 'DIVERSIFIED', topObservedSupplierSharePercentage: 6.3, supplierHhi: 389, supplierConcentration: 'DIVERSIFIED',
    },
    valueInterpretation: 'Average estimated value is moderate; large framework agreements skew the upper tail.',
    supplierInterpretation: 'No single supplier dominates observed awards; the market is competitive.',
    monthlyTrend: months.map((m, i) => ({ month: m, noticeCount: 520 + i * 40 + (i % 2) * 30, activeNoticeCount: 240 + i * 25, awardedNoticeCount: 150 + i * 18 })),
    topCpvs: [{ code: '45000000', description: 'Construction work', occurrenceCount: 760, sharePercentage: 18.4 }, { code: '45331000', description: 'HVAC installation work', occurrenceCount: 410, sharePercentage: 9.9 }, { code: '45321000', description: 'Thermal insulation work', occurrenceCount: 330, sharePercentage: 8 }, { code: '71320000', description: 'Engineering design services', occurrenceCount: 240, sharePercentage: 5.8 }, { code: '50700000', description: 'Repair and maintenance of building installations', occurrenceCount: 205, sharePercentage: 5 }],
    topBuyers: BUYERS.slice(0, 5).map((b, i) => ({ buyerId: b.id, name: b.name, countryCode: 'FR', noticeCount: 170 - i * 21, sharePercentage: Math.round((4.1 - i * 0.5) * 10) / 10 })),
    topSuppliers: SUPPLIERS.slice(0, 5).map((s, i) => ({ supplierKey: supplierSummary(s, i).supplierKey, name: s, observedAwardCount: 46 - i * 6, sharePercentage: Math.round((6.3 - i * 0.8) * 10) / 10 })),
    geographies: [{ countryCode: 'FR', regionCode: 'FR-ARA', regionName: 'Auvergne-Rhône-Alpes', noticeCount: 880, sharePercentage: 21.4 }, { countryCode: 'FR', regionCode: 'FR-OCC', regionName: 'Occitanie', noticeCount: 640, sharePercentage: 15.5 }, { countryCode: 'FR', regionCode: 'FR-IDF', regionName: 'Île-de-France', noticeCount: 590, sharePercentage: 14.3 }, { countryCode: 'FR', regionCode: 'FR-PAC', regionName: "Provence-Alpes-Côte d'Azur", noticeCount: 470, sharePercentage: 11.4 }],
    procedures: [{ value: 'OPEN', noticeCount: 2840, sharePercentage: 68.9 }, { value: 'RESTRICTED', noticeCount: 520, sharePercentage: 12.6 }, { value: 'COMPETITIVE_WITH_NEGOTIATION', noticeCount: 310, sharePercentage: 7.5 }, { value: 'OTHER', noticeCount: 450, sharePercentage: 11 }],
    contractNatures: [{ value: 'WORKS', noticeCount: 1950, sharePercentage: 47.3 }, { value: 'SERVICES', noticeCount: 1580, sharePercentage: 38.3 }, { value: 'SUPPLIES', noticeCount: 590, sharePercentage: 14.3 }],
    recentOpportunities: RAW.slice(0, 6).map((r) => ({ tenderId: r.id, buyerId: BUYERS[r.buyerIx].id, buyerName: BUYERS[r.buyerIx].name, title: r.title, status: r.status, procedure: r.procedure, contractNature: r.nature, publicationDate: ymd(r.pub), submissionDeadline: iso(r.dl, 12), estimatedMinValue: r.min, estimatedMaxValue: r.max, currency: 'EUR', awarded: false })),
  };
}

function dataOps() {
  const src = (name, health, f) => ({ source: name, health, runCount7Days: 14, successCount7Days: 14 - f, failureCount7Days: f, successRate7Days: Math.round(((14 - f) / 14) * 100), failureStreak: f > 3 ? 2 : 0, lastStartedAt: iso(-0.1), lastFinishedAt: iso(-0.09), lastSuccessAt: iso(f > 3 ? -3 : -0.09), lastFailureAt: f ? iso(-1) : null, rejectedCount7Days: 12 + f, importedCount7Days: 180 - f * 20 });
  const runs = [];
  for (let i = 0; i < 12; i++) runs.push({ source: ['TED', 'BOAMP', 'DECP'][i % 3], outcome: i === 4 ? 'FAILURE' : 'SUCCESS', rawStatus: i === 4 ? 'FAILED' : 'COMPLETED', startedAt: iso(-i / 4, 6), finishedAt: iso(-i / 4, 6), durationSeconds: 40 + i * 7, importedCount: 20 + i * 3, rejectedCount: i % 5 });
  return {
    generatedAt: new Date().toISOString(),
    summary: { sourceCount: 3, healthySourceCount: 2, degradedSourceCount: 0, unhealthySourceCount: 0, staleSourceCount: 1, runningSourceCount: 0, unknownSourceCount: 0, runs24Hours: 9, failures24Hours: 1, runs7Days: 42, successRate7Days: 97.6 },
    sources: [src('TED', 'HEALTHY', 0), src('BOAMP', 'HEALTHY', 1), src('DECP', 'STALE', 4)],
    recentRuns: runs,
  };
}

function sourceStatus(name) {
  const lastRun = { id: 'r1', status: 'COMPLETED', query: 'publication-date>=today(-7)', requestedLimit: 25, discoveredCount: 25, processedCount: 25, failedCount: 0, startedAt: iso(-0.1), completedAt: iso(-0.09), lastError: null };
  const base = { authenticationRequired: false, manualSyncEnabled: true, lastRun: lastRun };
  if (name === 'ted') return { source: 'TED', apiVersion: 'v3', baseUrl: 'https://api.ted.europa.eu', searchPath: '/v3/notices/search', canonicalNoticeCount: 1840, lastNoticeSeenAt: iso(-0.1), ...base };
  if (name === 'boamp') return { source: 'BOAMP', apiVersion: 'v2.1', baseUrl: 'https://boamp-datadila.opendatasoft.com', recordsPath: '/api/explore/v2.1/catalog/datasets/boamp/records', datasetId: 'boamp', canonicalNoticeCount: 2290, lastNoticeSeenAt: iso(-0.2), ...base };
  return { source: 'DECP', apiVersion: 'v2.1', baseUrl: 'https://data.economie.gouv.fr', recordsPath: '/api/explore/v2.1/catalog/datasets/decp/records', datasetId: 'decp', canonicalContractCount: 5120, lastContractSeenAt: iso(-3), ...base };
}

const WORKSPACE_DOCS = (tid) => [
  { id: 'doc1', category: 'TENDER_NOTICE', originalFilename: 'avis-de-marche.pdf', contentType: 'application/pdf', sizeBytes: 184220, sha256: 'ab12'.repeat(16), note: null, uploadedBy: user.fullName, uploadedAt: iso(-5) },
  { id: 'doc2', category: 'SPECIFICATIONS', originalFilename: 'CCTP-lots-1-3.pdf', contentType: 'application/pdf', sizeBytes: 2481920, sha256: 'cd34'.repeat(16), note: 'Latest revision', uploadedBy: user.fullName, uploadedAt: iso(-5) },
  { id: 'doc3', category: 'ADMINISTRATIVE', originalFilename: 'reglement-consultation.pdf', contentType: 'application/pdf', sizeBytes: 318400, sha256: 'ef56'.repeat(16), note: null, uploadedBy: user.fullName, uploadedAt: iso(-4) },
];
const REQS = (tid) => [
  { t: 'Valid ISO 9001 certification', c: 'ADMINISTRATIVE', s: 'SATISFIED', m: true, a: 'Amira Ben Salah' },
  { t: 'Three references of similar size in the last five years', c: 'TECHNICAL', s: 'SATISFIED', m: true, a: 'Karim Ouali' },
  { t: 'Annual turnover above €10M for the last three financial years', c: 'FINANCIAL', s: 'IN_PROGRESS', m: true, a: 'Amira Ben Salah' },
  { t: 'Technical memorandum with works methodology', c: 'TECHNICAL', s: 'IN_PROGRESS', m: true, a: 'Karim Ouali' },
  { t: 'Professional liability insurance certificate', c: 'LEGAL', s: 'GAP', m: true, a: 'Leïla Haddad' },
  { t: 'Environmental management plan', c: 'TECHNICAL', s: 'NOT_REVIEWED', m: false, a: null },
  { t: 'Price breakdown (DPGF) completed', c: 'COMMERCIAL', s: 'NOT_REVIEWED', m: true, a: 'Karim Ouali' },
  { t: 'Signed declaration of honour', c: 'ADMINISTRATIVE', s: 'SATISFIED', m: true, a: 'Amira Ben Salah' },
].map((r, i) => ({
  id: `req${i + 1}`, sourceDocumentId: i % 2 ? 'doc3' : 'doc2', sourceDocumentName: i % 2 ? 'reglement-consultation.pdf' : 'CCTP-lots-1-3.pdf',
  title: r.t, description: 'As required by the consultation rules.', category: r.c, status: r.s, mandatory: r.m, sourceReference: `Art. ${i + 4}.${(i % 3) + 1}`,
  dueDate: ymd(1 + i), assignedTo: r.a, evidenceNotes: r.s === 'SATISFIED' ? 'Evidence attached in the shared folder.' : null,
  createdBy: user.fullName, updatedBy: user.fullName, createdAt: iso(-5), updatedAt: iso(-1),
}));

// Documents and requirements deleted from a bid workspace stay deleted while the demo server runs.
const deletedWorkspaceItems = new Set();

// Requirement edits are kept too. As on the real server, an empty text clears a field and a field that
// is left out (or null) keeps its value.
const requirementPatches = new Map();

function patchedRequirement(tid, req) {
  const patch = requirementPatches.get(`${tid}:${req.id}`);
  if (!patch) return req;
  const out = { ...req };
  for (const key of ['title', 'category', 'status', 'mandatory', 'dueDate']) {
    if (patch[key] !== undefined) out[key] = patch[key];
  }
  for (const key of ['description', 'sourceReference', 'assignedTo', 'evidenceNotes']) {
    if (typeof patch[key] === 'string') out[key] = patch[key].trim().replace(/\s+/g, ' ') || null;
  }
  if (patch.sourceDocumentId) {
    out.sourceDocumentId = patch.sourceDocumentId;
    out.sourceDocumentName = WORKSPACE_DOCS(tid).find((d) => d.id === patch.sourceDocumentId)?.originalFilename ?? null;
  }
  return out;
}

function bidWorkspace(tid) {
  const r = RAW.find((x) => x.id === tid) ?? RAW[0];
  const reqs = REQS(tid).filter((x) => !deletedWorkspaceItems.has(`${tid}:${x.id}`)).map((x) => patchedRequirement(tid, x));
  const docs = WORKSPACE_DOCS(tid).filter((x) => !deletedWorkspaceItems.has(`${tid}:${x.id}`));
  const done = reqs.filter((x) => x.status === 'SATISFIED').length;
  return {
    tenderId: r.id, matchScore: r.score, matchGrade: r.grade, decisionOutcome: r.decision ?? 'PENDING', writable: !process.env.MOCK_LOCKED, writeLockReason: process.env.MOCK_LOCKED ? 'Bid workspace is locked because the bid decision is No-Go.' : null,
    opportunity: summaryOf(r),
    statistics: { documentCount: docs.length, requirementCount: reqs.length, completedRequirementCount: done, gapRequirementCount: reqs.filter((x) => x.status === 'GAP').length, mandatoryGapCount: reqs.filter((x) => x.status === 'GAP' && x.mandatory).length, completionPercentage: Math.round((done / reqs.length) * 100) },
    documents: docs, requirements: reqs,
  };
}

function compliance(tid) {
  const ws = bidWorkspace(tid);
  const verdicts = { SATISFIED: 'COMPLIANT', IN_PROGRESS: 'PARTIAL', GAP: 'NON_COMPLIANT', NOT_REVIEWED: 'UNASSESSED', NOT_APPLICABLE: 'NOT_APPLICABLE' };
  const risk = { COMPLIANT: 'LOW', PARTIAL: 'MEDIUM', NON_COMPLIANT: 'CRITICAL', UNASSESSED: 'UNASSESSED', NOT_APPLICABLE: 'UNASSESSED' };
  const rows = ws.requirements.map((q) => {
    const v = verdicts[q.status];
    return {
      requirementId: q.id, title: q.title, description: q.description, category: q.category, requirementStatus: q.status, mandatory: q.mandatory,
      sourceDocumentId: q.sourceDocumentId, sourceDocumentName: q.sourceDocumentName, sourceReference: q.sourceReference, dueDate: q.dueDate, assignedTo: q.assignedTo, evidenceNotes: q.evidenceNotes,
      verdict: v, riskLevel: risk[v], evidenceStrength: v === 'COMPLIANT' ? 'STRONG' : v === 'PARTIAL' ? 'MODERATE' : 'NONE', readinessContribution: v === 'COMPLIANT' ? 12 : v === 'PARTIAL' ? 6 : 0,
      priority: v === 'NON_COMPLIANT' ? 'BLOCKER' : v === 'PARTIAL' ? 'HIGH' : v === 'UNASSESSED' ? 'MEDIUM' : 'LOW',
      nextAction: v === 'COMPLIANT' ? 'No action required' : v === 'NON_COMPLIANT' ? 'Obtain the missing document before submission' : 'Complete the evidence and request review',
      reviewRationale: v === 'UNASSESSED' ? null : 'Reviewed against the consultation rules.', mitigationAction: v === 'NON_COMPLIANT' ? 'Request certificate from broker' : null,
      reviewedBy: v === 'UNASSESSED' ? null : user.fullName, reviewedAt: v === 'UNASSESSED' ? null : iso(-1),
    };
  });
  const count = (v) => rows.filter((x) => x.verdict === v).length;
  return {
    tenderId: tid, decisionOutcome: ws.decisionOutcome, writable: true, writeLockReason: null,
    statistics: { totalRequirements: rows.length, reviewedRequirements: rows.length - count('UNASSESSED'), mandatoryRequirements: rows.filter((x) => x.mandatory).length, compliantRequirements: count('COMPLIANT'), partialRequirements: count('PARTIAL'), nonCompliantRequirements: count('NON_COMPLIANT'), unassessedRequirements: count('UNASSESSED'), mandatoryBlockerCount: 1, mandatoryIncompleteCount: 3, criticalRiskCount: 1, readinessScore: 62, readyForSubmission: false },
    rows,
  };
}

// Task edits, moves and deletes are kept while the demo server runs.
const taskPatches = new Map();
const deletedTasks = new Set();

function planning(tid) {
  const r = RAW.find((x) => x.id === tid) ?? RAW[0];
  const bx = Math.max(0, RAW.findIndex((x) => x.id === tid));
  const T2 = (i, title, status0, priority, milestone, due, who) => {
    const patch = taskPatches.get(`${tid}:task${i}`) ?? {};
    const status = patch.status ?? status0;
    return {
    id: `task${i}`, requirementId: null, requirementTitle: null, title: patch.title ?? title, description: patch.description || null, status, priority: patch.priority ?? priority, milestone: patch.milestone ?? milestone, assignedTo: patch.assignedTo === undefined ? who : patch.assignedTo || null,
    dueDate: patch.dueDate ?? ymd(due), overdue: status !== 'DONE' && status !== 'CANCELLED' && (patch.dueDate ?? ymd(due)) < ymd(0), completedAt: status === 'DONE' ? iso(-2) : null, createdBy: user.fullName, updatedBy: user.fullName, createdAt: iso(-8), updatedAt: iso(-1),
    };
  };
  const allTasks = [
    T2(1, 'Kick-off meeting and bid/no-bid recap', 'DONE', 'MEDIUM', 'KICKOFF', -7, 'Amira Ben Salah'),
    T2(2, 'Review tender documents and extract requirements', 'DONE', 'HIGH', 'DOCUMENT_REVIEW', -5, 'Karim Ouali'),
    T2(3, 'Collect certificates and references', 'IN_PROGRESS', 'HIGH', 'COMPLIANCE', 1, 'Leïla Haddad'),
    T2(4, 'Draft technical memorandum', 'IN_PROGRESS', 'CRITICAL', 'TECHNICAL_RESPONSE', bx % 2 === 1 ? -2 : 1, 'Karim Ouali'),
    T2(5, 'Insurance certificate from broker', bx % 2 === 1 ? 'IN_PROGRESS' : 'BLOCKED', 'CRITICAL', 'COMPLIANCE', bx % 2 === 1 ? 2 : -1, 'Leïla Haddad'),
    T2(6, 'Build price breakdown (DPGF)', 'TODO', 'HIGH', 'COMMERCIAL_RESPONSE', 2, 'Karim Ouali'),
    T2(7, 'Internal review and approval', 'TODO', 'MEDIUM', 'INTERNAL_REVIEW', 3, null),
    T2(8, 'Upload and submit on the buyer portal', 'TODO', 'CRITICAL', 'SUBMISSION', 4, 'Amira Ben Salah'),
    T2(9, 'Request a second site visit', 'CANCELLED', 'LOW', 'OTHER', 5, null),
  ];
  allTasks[2].description = 'ISO 9001, insurance and three references from the last five years.';
  allTasks[3].description = 'Methodology, planning and team CVs for the technical response.';
  allTasks[3].requirementTitle = 'Technical memorandum with works methodology';
  for (const t of allTasks) {
    const patched = taskPatches.get(`${tid}:${t.id}`);
    if (patched && patched.description !== undefined) t.description = patched.description || null;
  }
  const tasks = allTasks.filter((t) => !deletedTasks.has(`${tid}:${t.id}`));
  const isOpen = (t) => t.status !== 'DONE' && t.status !== 'CANCELLED';
  const doneCount = tasks.filter((t) => t.status === 'DONE').length;
  return {
    tenderId: r.id, decisionOutcome: r.decision ?? 'GO', complianceReady: false, complianceReadinessScore: 62, writable: !process.env.MOCK_LOCKED, writeLockReason: process.env.MOCK_LOCKED ? 'Planning is locked because the bid decision is No-Go.' : null,
    statistics: { readinessScore: 58, totalTasks: tasks.length, openTasks: tasks.filter(isOpen).length, completedTasks: doneCount, blockedTasks: tasks.filter((t) => t.status === 'BLOCKED').length, overdueTasks: tasks.filter((t) => t.overdue).length, criticalOpenTasks: tasks.filter((t) => isOpen(t) && t.priority === 'CRITICAL').length, taskCompletionPercentage: Math.round((doneCount / tasks.length) * 100), checklistCompletionPercentage: 50, readyForSubmission: false },
    submissionControl: { portalReady: true, signaturesReady: false, technicalResponseReady: false, commercialResponseReady: false, administrativePackReady: true, finalReviewReady: false, submissionAuthorized: false, plannedSubmissionAt: iso(r.dl - 1, 10), submittedAt: null, submissionReference: null, submittedBy: null, notes: 'Submission window opens 48h before deadline.', updatedBy: user.fullName },
    tasks,
    events: [
      { id: 'e1', eventType: 'TASK_UPDATED', taskId: 'task4', actor: 'Karim Ouali', detail: 'Moved “Draft technical memorandum” to IN_PROGRESS', createdAt: iso(-0.3) },
      { id: 'e2', eventType: 'TASK_CREATED', taskId: 'task8', actor: 'Amira Ben Salah', detail: 'Created task “Upload and submit on the buyer portal”', createdAt: iso(-1) },
    ],
  };
}

function decisionWorkspace(tid) {
  const r = RAW.find((x) => x.id === tid) ?? RAW[0];
  const q = queueItem(r);
  return {
    tenderId: r.id, matchScore: r.score, matchGrade: r.grade, matchBlocked: !!r.blocked, opportunity: summaryOf(r),
    decision: {
      id: q.decision?.id ?? 'd-new', outcome: r.decision ?? 'PENDING', recommendedOutcome: r.score >= 75 ? 'GO' : r.score >= 55 ? 'HOLD' : 'NO_GO', decisionScore: Math.round(r.score * 0.92), matchScoreSnapshot: r.score, matchGradeSnapshot: r.grade,
      strategicFit: 4, capabilityConfidence: 4, commercialAttractiveness: 3, deliveryFeasibility: 4, resourceCapacity: 3, riskConfidence: 3,
      recommendationReason: 'High CPV and regional fit with strong references; resource capacity is the main constraint.',
      rationale: r.decision === 'GO' ? 'Strategic account with repeat buyer; we have available capacity from November.' : null,
      keyRisks: 'Tight deadline; insurance certificate still outstanding.', assumptions: 'Subcontracting the electrical lot to a trusted partner.',
      decidedBy: r.decision ? user.fullName : null, decidedAt: r.decision ? iso(-2) : null, createdAt: iso(-5), updatedAt: iso(-1),
    },
    history: [
      { id: 'h1', eventType: 'OUTCOME_CHANGED', previousOutcome: 'PENDING', newOutcome: r.decision ?? 'PENDING', decisionScore: Math.round(r.score * 0.92), actor: user.fullName, note: 'Approved in the weekly bid committee.', createdAt: iso(-2) },
      { id: 'h2', eventType: 'ASSESSMENT_UPDATED', previousOutcome: 'PENDING', newOutcome: 'PENDING', decisionScore: 70, actor: 'Karim Ouali', note: null, createdAt: iso(-4) },
    ],
  };
}

export function handle(method, pathname, search = '', body = null) {
  const q = new URLSearchParams(search);
  const ok = (json) => ({ status: 200, json });
  if (pathname.startsWith('/api/v1/auth/')) {
    if (pathname.endsWith('/logout')) return { status: 204 };
    if (pathname.endsWith('/me')) return ok(user);
    return ok({ accessToken: 'mock-token', expiresInSeconds: 900, user });
  }
  if (pathname === '/api/v1/system/status') return ok({ application: 'tenderpilot-backend', version: '1.0.0', environment: 'mock', timestamp: new Date().toISOString(), dependencies: [{ name: 'PostgreSQL', status: 'UP', detail: 'Mock' }, { name: 'OpenSearch', status: 'UP', detail: 'Mock' }] });
  if (pathname === '/api/v1/dashboard/command-center') {
    const d = dashboard();
    if (process.env.MOCK_FRESH) {
      for (const k of Object.keys(d.summary)) d.summary[k] = 0;
      d.portfolio = []; d.priorities = [];
    }
    return ok(d);
  }
  if (pathname === '/api/v1/notifications/watchlist' && method === 'POST') {
    const type = body?.type;
    const raw = String(body?.targetKey ?? '').trim();
    const key = type === 'CPV' ? raw.replace(/\s+/g, '') : raw.toLowerCase();
    const valid = type === 'CPV' ? /^[0-9]{1,8}$/.test(key) : /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/.test(key);
    if (!['TENDER', 'BUYER', 'CPV'].includes(type) || !valid) {
      return { status: 400, json: { message: type === 'CPV' ? 'CPV watchlist targets must contain between 1 and 8 digits' : 'Watchlist target must be a valid UUID' } };
    }
    if (!watchlist().some((item) => item.type === type && item.targetKey === key)) {
      watchlist().unshift({ id: `w${Date.now().toString(16).padStart(7, '0').slice(-7)}-0000-4000-8000-000000000000`, type, targetKey: key, label: body?.label ?? null, createdAt: new Date().toISOString() });
    }
    return ok(notifications());
  }
  const watchDelete = method === 'DELETE' ? pathname.match(/^\/api\/v1\/notifications\/watchlist\/([^/]+)$/) : null;
  if (watchDelete) {
    watchlistItems = watchlist().filter((item) => item.id !== watchDelete[1]);
    return ok(notifications());
  }
  if (pathname === '/api/v1/notifications' && method === 'GET') return ok(notifications());
  const alertAction = method === 'POST' ? pathname.match(/^\/api\/v1\/notifications\/([^/]+)\/(acknowledge|dismiss)$/) : null;
  if (alertAction) {
    const current = notifications().notifications.find((x) => x.id === alertAction[1]);
    if (current) {
      notificationState.set(alertAction[1], alertAction[2] === 'dismiss'
        ? { status: 'DISMISSED', active: false }
        : { status: current.status === 'DISMISSED' ? 'DISMISSED' : 'READ', active: current.active });
    }
    return ok(notifications());
  }
  if (method === 'POST' && pathname === '/api/v1/notifications/read-all') {
    for (const x of notifications().notifications) if (x.status === 'UNREAD') notificationState.set(x.id, { status: 'READ', active: x.active });
    return ok(notifications());
  }
  if (method === 'POST' && pathname === '/api/v1/notifications/refresh') {
    for (const x of notifications().notifications) if (x.status === 'DISMISSED') notificationState.set(x.id, { status: 'DISMISSED', active: true });
    return ok(notifications());
  }
  if (pathname.startsWith('/api/v1/notifications')) return ok(notifications());
  if (pathname === '/api/v1/company/profile') return ok(company());
  if (pathname === '/api/v1/organizations/current') return ok({ id: user.organization.id, name: user.organization.name, slug: user.organization.slug, createdAt: iso(-60) });
  if (pathname === '/api/v1/organizations/current/members')
    return ok([
      { userId: user.id, fullName: user.fullName, email: user.email, role: 'OWNER', joinedAt: iso(-60) },
      { userId: 'u2', fullName: 'Karim Ouali', email: 'karim@atlasbuild.example', role: 'BID_MANAGER', joinedAt: iso(-45) },
      { userId: 'u3', fullName: 'Leïla Haddad', email: 'leila@atlasbuild.example', role: 'ANALYST', joinedAt: iso(-30) },
      { userId: 'u4', fullName: 'Sami Trabelsi', email: 'sami@atlasbuild.example', role: 'CONTRIBUTOR', joinedAt: iso(-12) },
    ]);

  if (pathname === '/api/v1/opportunities') {
    let list = RAW.map(summaryOf);
    const text = (q.get('q') ?? '').toLowerCase();
    if (text) list = list.filter((o) => (o.title + ' ' + o.buyerName).toLowerCase().includes(text));
    const st = q.get('status');
    if (st) list = list.filter((o) => o.status === st);
    const src = q.get('source');
    if (src) list = list.filter((o) => o.sources.includes(src));
    const mq = q.get('minQualityScore');
    if (mq) list = list.filter((o) => (o.dataQualityScore ?? 0) >= Number(mq));
    return ok(list);
  }
  let m;
  if ((m = pathname.match(/^\/api\/v1\/opportunities\/(.+)$/))) {
    const r = RAW.find((x) => x.id === m[1]) ?? RAW[0];
    return ok(detailOf(r));
  }
  if (pathname === '/api/v1/search/status') return ok({ enabled: true, reachable: true, indexExists: true, indexName: 'tenderpilot-opportunities-v1', indexedDocuments: 4120, authority: 'POSTGRESQL', fallbackEngine: 'POSTGRESQL' });
  if (pathname === '/api/v1/search/opportunities') {
    let list = RAW.map(summaryOf);
    const text = (q.get('q') ?? q.get('query') ?? '').toLowerCase();
    if (text) list = list.filter((o) => (o.title + ' ' + o.buyerName).toLowerCase().includes(text));
    // MOCK_MORE=1 pretends the index holds many more hits than the page returns,
    // so the "Show more" button and its limit message can be seen.
    const size = Math.min(100, Math.max(1, Number(q.get('size') ?? 25)));
    const total = process.env.MOCK_MORE ? Math.max(list.length, 140) : list.length;
    return ok({ engine: 'OPENSEARCH', fallback: false, total, hits: list.slice(0, size).map((o, i) => ({ score: 12 - i * 0.7, opportunity: o })) });
  }
  if (pathname === '/api/v1/matches/overview') return ok({ totalMatches: matches.length, actionableMatches: 9, blockedMatches: 1, excellentMatches: 2, strongMatches: 3, goodMatches: 4, averageScore: 66, lastEvaluatedAt: matchesEvaluatedAt ?? (process.env.MOCK_STALE ? iso(-2) : iso(-0.1)) });
  if (pathname === '/api/v1/matches/recalculate') {
    matchesEvaluatedAt = new Date().toISOString();
    return ok({ evaluatedCount: 12, actionableCount: 9, blockedCount: 1, excellentCount: 2, strongCount: 3 });
  }
  if (pathname === '/api/v1/matches') return ok(matches);
  if (pathname === '/api/v1/bid-decisions/overview') return ok({ candidateCount: 12, totalDecisions: decisions.length, pendingCount: 2, goCount: 2, holdCount: 1, noGoCount: 1, averageDecisionScore: 71, lastUpdatedAt: iso(-1) });
  if (pathname === '/api/v1/bid-decisions/queue') return ok(RAW.slice().sort((a, b) => b.score - a.score).map(queueItem));
  if ((m = pathname.match(/^\/api\/v1\/bid-decisions\/([^/]+)/))) return ok(decisionWorkspace(m[1]));
  if (method === 'PUT' && (m = pathname.match(/^\/api\/v1\/bid-workspaces\/([^/]+)\/requirements\/([^/]+)$/))) {
    const key = `${m[1]}:${m[2]}`;
    const given = Object.fromEntries(Object.entries(body ?? {}).filter(([, value]) => value !== null && value !== undefined));
    requirementPatches.set(key, { ...(requirementPatches.get(key) ?? {}), ...given });
    return ok(bidWorkspace(m[1]));
  }
  if (method === 'DELETE' && (m = pathname.match(/^\/api\/v1\/bid-workspaces\/([^/]+)\/(documents|requirements)\/([^/]+)$/))) {
    deletedWorkspaceItems.add(`${m[1]}:${m[3]}`);
    return ok(bidWorkspace(m[1]));
  }
  if ((m = pathname.match(/^\/api\/v1\/bid-workspaces\/([^/]+)$/))) return ok(bidWorkspace(m[1]));
  if ((m = pathname.match(/^\/api\/v1\/compliance\/bid-workspaces\/([^/]+)/))) return ok(compliance(m[1]));
  if ((method === 'PUT' || method === 'DELETE') && (m = pathname.match(/^\/api\/v1\/bid-planning\/([^/]+)\/tasks\/([^/]+)$/))) {
    const key = `${m[1]}:${m[2]}`;
    if (method === 'DELETE') deletedTasks.add(key);
    else if (body) taskPatches.set(key, { ...(taskPatches.get(key) ?? {}), ...body });
    return ok(planning(m[1]));
  }
  if ((m = pathname.match(/^\/api\/v1\/bid-planning\/([^/]+)/))) return ok(planning(m[1]));
  if (pathname === '/api/v1/local-ai/status') return ok({ enabled: true, reachable: true, modelAvailable: true, provider: 'OLLAMA', model: 'qwen3:4b', baseUrl: 'http://localhost:11434', mode: 'LOCAL_ONLY', dataBoundary: 'ORGANIZATION_PRIVATE' });
  if (pathname.includes('/local-ai/bid-workspaces/') && pathname.endsWith('/analyses'))
    return ok([{ documentId: 'doc2', filename: 'CCTP-lots-1-3.pdf', documentCategory: 'SPECIFICATIONS', status: 'COMPLETED', provider: 'OLLAMA', model: 'qwen3:4b', promptVersion: 'BID_DOCUMENT_ANALYSIS_V1', confidence: 0.82, summary: 'Technical specifications for a three-lot energy retrofit with strict commissioning requirements and a 14-month execution window.', errorMessage: null, extractedCharacterCount: 84210, extractionTruncated: false, detectedMediaType: 'application/pdf', analyzedAt: iso(-1), findings: [
      { type: 'REQUIREMENT', title: 'ISO 9001 certification required', detail: 'Tenderers must hold a valid ISO 9001 certificate.', evidenceSnippet: 'Le candidat justifie d’une certification ISO 9001 en cours de validité.', mandatory: true, requirementCategory: 'ADMINISTRATIVE' },
      { type: 'RISK', title: 'Liquidated damages for delay', detail: 'Penalties of 1/1000 per day of delay.', evidenceSnippet: 'Une pénalité de 1/1000 du montant du marché par jour calendaire…', mandatory: null, requirementCategory: null },
      { type: 'DEADLINE', title: 'Site visit mandatory', detail: 'A site visit must be completed before submission.', evidenceSnippet: null, mandatory: true, requirementCategory: 'SUBMISSION' },
    ] }, { documentId: 'doc1', filename: 'avis-de-marche.pdf', documentCategory: 'TENDER_NOTICE', status: 'NOT_ANALYZED', provider: 'OLLAMA', model: 'qwen3:4b', promptVersion: 'BID_DOCUMENT_ANALYSIS_V1', confidence: null, summary: null, errorMessage: null, extractedCharacterCount: null, extractionTruncated: null, detectedMediaType: null, analyzedAt: null, findings: [] }]);
  if (pathname === '/api/v1/rag/status') return ok({ enabled: true, reachable: true, generationModelAvailable: true, embeddingModelAvailable: true, generationModel: 'qwen3:4b', embeddingModel: 'nomic-embed-text-v2-moe', baseUrl: 'http://localhost:11434', vectorStore: 'PostgreSQL', dataBoundary: 'ORGANIZATION_PRIVATE' });
  if (pathname.endsWith('/index') && pathname.includes('/rag/')) return ok({ documentCount: 3, indexedDocumentCount: 3, chunkCount: 142, ready: true, embeddingModel: 'nomic-embed-text-v2-moe' });
  if (pathname.endsWith('/history') && pathname.includes('/rag/'))
    return ok([{ id: 'qa1', question: 'What is the maximum duration of the works?', answer: 'The works must be completed within 14 months of the service order, including commissioning.', supported: true, generationModel: 'qwen3:4b', embeddingModel: 'nomic-embed-text-v2-moe', createdAt: iso(-1), citations: [{ sourceId: 'S1', documentId: 'doc2', filename: 'CCTP-lots-1-3.pdf', chunkIndex: 12, similarity: 0.71, excerpt: 'La durée d’exécution des travaux est fixée à quatorze (14) mois à compter de l’ordre de service…' }] }]);
  if (pathname === '/api/v1/buyers') {
    const text = (q.get('q') ?? '').toLowerCase();
    return ok(BUYERS.map(buyerSummary).filter((b) => !text || b.name.toLowerCase().includes(text)));
  }
  if ((m = pathname.match(/^\/api\/v1\/buyers\/(.+)$/))) {
    const ix = Math.max(0, BUYERS.findIndex((b) => b.id === m[1]));
    return ok({
      summary: buyerSummary(BUYERS[ix], ix),
      topCpvs: [{ code: '45331000', description: 'HVAC installation work', occurrenceCount: 18, sharePercentage: 32 }, { code: '45321000', description: 'Thermal insulation work', occurrenceCount: 11, sharePercentage: 19 }, { code: '45454000', description: 'Restructuring work', occurrenceCount: 7, sharePercentage: 12 }],
      topSuppliers: SUPPLIERS.slice(0, 4).map((s, i) => ({ name: s, awardCount: 9 - i * 2, sharePercentage: 24 - i * 5 })),
      monthlyActivity: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((mo) => ({ month: mo, noticeCount: [3, 5, 8, 6, 4, 2, 1, 2, 7, 9, 6, 4][mo - 1] })),
      recentTenders: [...new Set(RAW.filter((r) => r.buyerIx === ix).concat(RAW.slice(0, 3)))].slice(0, 4).map((r) => ({ tenderId: r.id, title: r.title, status: r.status, publicationDate: ymd(r.pub), submissionDeadline: iso(r.dl, 12), estimatedMinValue: r.min, estimatedMaxValue: r.max, currency: 'EUR' })),
    });
  }
  if (pathname === '/api/v1/suppliers') return ok(SUPPLIERS.map(supplierSummary));
  if ((m = pathname.match(/^\/api\/v1\/suppliers\/(.+)$/))) {
    // A key is either the demo key or the supplier name encoded the way the real backend does it.
    const named = Buffer.from(m[1], 'base64url').toString('utf8');
    const ix = Math.max(0, SUPPLIERS.findIndex((s) => supplierSummary(s, 0).supplierKey === m[1] || s === named));
    return ok({
      summary: supplierSummary(SUPPLIERS[ix], ix),
      valueInterpretation: 'Associated award values reflect the full contract value of awards where the supplier was observed.',
      competitiveLandscapeInterpretation: 'The supplier wins repeatedly from a small group of buyers, suggesting established relationships.',
      topBuyers: BUYERS.slice(0, 4).map((b, i) => ({ buyerId: b.id, name: b.name, countryCode: 'FR', city: b.city, awardCount: 9 - i * 2, sharePercentage: 26 - i * 5 })),
      topCpvs: [{ code: '45331000', description: 'HVAC installation work', occurrenceCount: 12, sharePercentage: 38 }, { code: '45321000', description: 'Thermal insulation work', occurrenceCount: 8, sharePercentage: 24 }],
      coAwardSuppliers: SUPPLIERS.slice(1, 4).map((s, i) => ({ supplierKey: supplierSummary(s, i + 1).supplierKey, name: s, sharedAwardCount: 4 - i, sharePercentage: 12 - i * 3 })),
      recentAwards: RAW.slice(0, 4).map((r) => ({ awardId: 'aw-' + r.id, tenderId: r.id, tenderTitle: r.title, tenderStatus: 'AWARDED', buyerId: BUYERS[r.buyerIx].id, buyerName: BUYERS[r.buyerIx].name, buyerCountryCode: 'FR', buyerCity: BUYERS[r.buyerIx].city, publicationDate: ymd(r.pub), associatedAwardValue: r.min, awardCurrency: 'EUR', jointAward: false })),
    });
  }
  if (pathname === '/api/v1/market/explorer') return ok(market());
  if (pathname === '/api/v1/data-operations') return ok(dataOps());
  if (pathname === '/api/v1/data-quality/overview') return ok({ totalTenders: 4120, assessedTenders: 4086, unassessedTenders: 34, averageScore: 76.4, excellentCount: 1210, goodCount: 1640, fairCount: 870, poorCount: 290, criticalCount: 76, criticalIssueCount: 112, warningIssueCount: 1304, infoIssueCount: 2210, lastAssessedAt: iso(-0.2) });
  if (pathname === '/api/v1/data-quality/recalculate') return ok({ recalculatedCount: 4120 });
  if ((m = pathname.match(/^\/api\/v1\/data-sources\/(ted|boamp|decp)(\/sync)?$/))) {
    if (m[2]) return ok({ runId: 'run-mock', status: 'COMPLETED', query: 'publication-date>=today(-7)', requestedLimit: 25, discoveredCount: 25, processedCount: 25, failedCount: 0, startedAt: iso(0), completedAt: iso(0), lastError: null });
    return ok(sourceStatus(m[1]));
  }
  return { status: 404, json: { message: 'Not mocked: ' + method + ' ' + pathname } };
}

export const fixtures = { RAW, BUYERS, SUPPLIERS };
