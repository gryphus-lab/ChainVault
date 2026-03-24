/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Combines class names intelligently.
 * Merges Tailwind classes and resolves conflicts.
 *
 * Usage:
 *   cn("text-red-500", isActive && "font-bold", "mt-4")
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
