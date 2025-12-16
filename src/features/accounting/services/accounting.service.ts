import axios from 'axios';
import { TaxReconciliationDetail } from '../types/accounting.types';

// Assuming global axios instance or base URL configuration exists. 
// If not, we use relative /api/v1...

export const getTaxReconciliation = async (period: string): Promise<TaxReconciliationDetail[]> => {
    const { data } = await axios.get(`/api/v1/audit/reconciliation`, {
        params: { period }
    });
    return data;
};
