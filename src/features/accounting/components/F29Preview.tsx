import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { F29Report } from "@/features/accounting/types/accounting.types";

interface F29PreviewProps {
    report: F29Report;
}

export function F29Preview({ report }: F29PreviewProps) {
    const formatCLP = (amount: number) => {
        return new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(amount);
    };

    return (
        <div className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium">Ventas (Débito)</CardTitle>
                    </CardHeader>
                    <CardContent>
                        {/* 🔧 Actualizar referencia a .totalSales */}
                        <div className="text-2xl font-bold">{formatCLP(report.totalSales)}</div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium">Compras (Crédito)</CardTitle>
                    </CardHeader>
                    <CardContent>
                        {/* 🔧 Actualizar referencia a .totalPurchases */}
                        <div className="text-2xl font-bold">{formatCLP(report.totalPurchases)}</div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium">IVA Determinado</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{formatCLP(report.vatPayable)}</div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium">Total a Pagar</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{formatCLP(report.totalPayable)}</div>
                    </CardContent>
                </Card>
            </div>

            <Card className="col-span-4">
                <CardHeader>
                    <CardTitle>Detalle de Líneas (Formulario 29)</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="space-y-2">
                        {report.details && report.details.length > 0 ? (
                            report.details.map((line) => (
                                <div key={line.code} className="flex justify-between items-center border-b pb-2">
                                    <div className="flex items-center gap-2">
                                        <div className="bg-slate-100 text-slate-800 font-mono text-xs px-2 py-1 rounded">
                                            Code {line.code}
                                        </div>
                                        <span className="text-sm font-medium">{line.description}</span>
                                    </div>
                                    <span className="font-bold">{formatCLP(line.amount)}</span>
                                </div>
                            ))
                        ) : (
                            <div className="text-muted-foreground text-sm">No hay líneas generadas para este periodo.</div>
                        )}
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
