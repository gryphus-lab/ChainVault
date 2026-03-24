/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { cn } from "../../lib/utils";
import * as React from "react";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {}

export function Card({ className, ...props }: Readonly<CardProps>) {
  return (
    <div
      className={cn("bg-white shadow rounded-lg overflow-hidden", className)}
      {...props}
    />
  );
}

export function CardHeader({ className, ...props }: Readonly<CardProps>) {
  return (
    <div
      className={cn("px-6 py-5 border-b border-gray-200", className)}
      {...props}
    />
  );
}

export function CardTitle({ className, ...props }: Readonly<CardProps>) {
  return (
    <h3
      className={cn("text-lg font-medium text-gray-900", className)}
      {...props}
    />
  );
}

export function CardContent({ className, ...props }: Readonly<CardProps>) {
  return <div className={cn("px-6 py-5", className)} {...props} />;
}
