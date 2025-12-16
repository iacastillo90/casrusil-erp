import React from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { CalendarIcon } from "lucide-react";
import { cn } from "@/lib/utils";

interface PeriodSelectorProps {
  selectedMonth: number; // 1-12
  selectedYear: number;
  onChange: (month: number, year: number) => void;
  className?: string;
}

const MONTHS = [
  { value: 1, label: "Enero" },
  { value: 2, label: "Febrero" },
  { value: 3, label: "Marzo" },
  { value: 4, label: "Abril" },
  { value: 5, label: "Mayo" },
  { value: 6, label: "Junio" },
  { value: 7, label: "Julio" },
  { value: 8, label: "Agosto" },
  { value: 9, label: "Septiembre" },
  { value: 10, label: "Octubre" },
  { value: 11, label: "Noviembre" },
  { value: 12, label: "Diciembre" },
];

export const PeriodSelector: React.FC<PeriodSelectorProps> = ({
  selectedMonth,
  selectedYear,
  onChange,
  className,
}) => {
  const currentYear = new Date().getFullYear();
  // Generate a range of years: Current - 5 to Current + 1
  const years = Array.from({ length: 7 }, (_, i) => currentYear - 5 + i);

  const handleMonthChange = (value: string) => {
    onChange(Number(value), selectedYear);
  };

  const handleYearChange = (value: string) => {
    onChange(selectedMonth, Number(value));
  };

  return (
    <div className={cn("flex items-center space-x-2 bg-background p-1 rounded-md border", className)}>
      <div className="px-2 text-muted-foreground">
        <CalendarIcon className="h-4 w-4" />
      </div>
      
      {/* Month Selector */}
      <Select value={selectedMonth.toString()} onValueChange={handleMonthChange}>
        <SelectTrigger className="w-[120px] border-none shadow-none focus:ring-0 h-8">
          <SelectValue placeholder="Mes" />
        </SelectTrigger>
        <SelectContent>
          {MONTHS.map((month) => (
            <SelectItem key={month.value} value={month.value.toString()}>
              {month.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <span className="text-border">|</span>

      {/* Year Selector */}
      <Select value={selectedYear.toString()} onValueChange={handleYearChange}>
        <SelectTrigger className="w-[90px] border-none shadow-none focus:ring-0 h-8 font-medium">
          <SelectValue placeholder="Año" />
        </SelectTrigger>
        <SelectContent>
          {years.map((year) => (
            <SelectItem key={year} value={year.toString()}>
              {year}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
};
