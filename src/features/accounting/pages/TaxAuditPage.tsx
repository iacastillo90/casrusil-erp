import React, { useState } from "react";
import { PeriodSelector } from "@/components/shared/PeriodSelector";
import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { RefreshCw, Download, AlertTriangle, CheckCircle } from "lucide-react";

// Mock API service for demonstration
const mockGetSiiMirrorReport = async (month: number, year: number) => {
    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 1000));
    return {
        period: `${month}/${year}`,
        status: "MATCH",
        proposalF29: 1560000,
        erpBalance: 1560000,
        differences: [],
    };
};

export default function TaxAuditPage() {
    // Initialize state with current month/year
    const [period, setPeriod] = useState({
        month: new Date().getMonth() + 1,
        year: new Date().getFullYear(),
    });

    // Query that depends on the selected period
    const { data, isLoading, refetch, isRefetching } = useQuery({
        queryKey: ["tax-audit", period.month, period.year],
        queryFn: () => mockGetSiiMirrorReport(period.month, period.year),
    });

    const handlePeriodChange = (month: number, year: number) => {
        setPeriod({ month, year });
        // TanStack Query handles the refetch automatically due to dependency change
    };

    return (
        <div className="flex flex-col h-full w-full space-y-6 p-8">
            {/* Header with Title and Period Selector */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Auditoría Tributaria</h1>
                    <p className="text-muted-foreground mt-1">
                        Conciliación automática entre SII y registros contables (F29).
                    </p>
                </div>

                <div className="flex items-center gap-2">
                    <Button
                        variant="outline"
                        size="icon"
                        onClick={() => refetch()}
                        disabled={isLoading || isRefetching}
                        className={isRefetching ? "animate-spin" : ""}
                    >
                        <RefreshCw className="h-4 w-4" />
                    </Button>

                    {/* Period Selector Component Integrated Here */}
                    <PeriodSelector
                        selectedMonth={period.month}
                        selectedYear={period.year}
                        onChange={handlePeriodChange}
                    />
                </div>
            </div>

            {/* Main Content Area */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {/* Status Card */}
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Estado de Conciliación</CardTitle>
                        {data?.status === "MATCH" ? (
                            <CheckCircle className="h-4 w-4 text-green-500" />
                        ) : (
                            <AlertTriangle className="h-4 w-4 text-orange-500" />
                        )}
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {isLoading ? "Cargando..." : (data?.status === "MATCH" ? "Cuadrado" : "Diferencias")}
                        </div>
                        <p className="text-xs text-muted-foreground">
                            Periodo {period.month}/{period.year}
                        </p>
                    </CardContent>
                </Card>

                {/* F29 Proposal Card */}
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Propuesta F29 (SII)</CardTitle>
                        <span className="text-xs font-bold text-muted-foreground">CLP</span>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {isLoading ? "..." : `$ ${data?.proposalF29?.toLocaleString()}`}
                        </div>
                        <p className="text-xs text-muted-foreground">IVA Débito + Crédito Fiscal</p>
                    </CardContent>
                </Card>

                {/* ERP Balance Card */}
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Registro ERP</CardTitle>
                        <span className="text-xs font-bold text-muted-foreground">CLP</span>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {isLoading ? "..." : `$ ${data?.erpBalance?.toLocaleString()}`}
                        </div>
                        <p className="text-xs text-muted-foreground">Libros Compra/Venta Internos</p>
                    </CardContent>
                </Card>
            </div>

            {/* Placeholder for detailed table */}
            <Card className="flex-1">
                <CardHeader>
                    <CardTitle>Detalle de Movimientos</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="flex items-center justify-center p-8 text-muted-foreground border-dashed border-2 rounded-lg">
                        Seleccione un periodo para ver el detalle de facturas y boletas.
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
