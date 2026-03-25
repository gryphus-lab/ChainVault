/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { type ClassValue } from "clsx";
import { cn } from "./utils";
describe("TestUtils", () => {
  it("returns an empty string when no arguments are provided", () => {
    const inputs: ClassValue[] = [];
    expect(cn(...inputs)).toBe("");
  });
});
