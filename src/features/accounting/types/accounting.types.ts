export interface F29Report {
    period: string;
    totalSales: number;      // 🔧 Renombrado de 'sales' (y cambiado a number que es lo real)
    totalPurchases: number;  // 🔧 Renombrado de 'purchases'
    vatPayable: number;      // Ajustado a number para consistencia
    vatRecoverable: number;
    feeWithholding: number;
    totalPayable: number;
    details: {
        code: string;
        description: string;
        amount: number;      // Ajustado a number
    }[];
}

export type ReconciliationStatus = 'MATCH' | 'MISSING_IN_ERP' | 'MISSING_IN_SII' | 'MISMATCH';

export interface TaxReconciliationDetail {
    id: string;
    period: string;
    documentType: string;
    folio: number;
    counterpartRut: string;
    counterpartName: string; // ✅ Razón Social
    amountSii: number;
    amountErp: number;
    status: ReconciliationStatus;
    difference: number;
}

export interface AuditStats {
    totalSii: number;
    totalErp: number;
    matchRate: number;
    docCount: number;
}
