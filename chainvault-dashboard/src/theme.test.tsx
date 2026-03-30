import { describe, it, expect } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { tokens, themeSettings, useMode } from "./theme";

describe("Theme Logic", () => {
  it("should return dark mode tokens when mode is dark", () => {
    const darkTokens = tokens("dark");
    expect(darkTokens.primary[500]).toBe("#141b2d");
    expect(darkTokens.gray[100]).toBe("#e0e0e0");
  });

  it("should return light mode tokens when mode is light", () => {
    const lightTokens = tokens("light");
    // In your light mode logic: primary[100] is #040509
    expect(lightTokens.primary[100]).toBe("#040509");
    expect(lightTokens.gray[100]).toBe("#141414");
  });

  it("should generate correct MUI theme settings for dark mode", () => {
    const settings = themeSettings("dark");
    expect(settings.palette.mode).toBe("dark");
    expect(settings.palette.primary.main).toBe("#141b2d");
  });
});

describe("useMode Hook", () => {
  it("should initialize with dark mode", () => {
    const { result } = renderHook(() => useMode());
    // Destructure the tuple here
    const [theme] = result.current;

    expect(theme.palette.mode).toBe("dark");
  });

  it("should toggle between light and dark mode", () => {
    const { result } = renderHook(() => useMode());

    // 1. Check initial theme
    expect(result.current[0].palette.mode).toBe("dark");

    // 2. Execute toggle on the second element of the array
    act(() => {
      result.current[1].toggleColorMode();
    });

    // 3. Check updated theme
    expect(result.current[0].palette.mode).toBe("light");
  });

  it("should maintain a stable colorMode object reference", () => {
    const { result, rerender } = renderHook(() => useMode());
    // Store the colorMode object (index 1)
    const firstRenderColorMode = result.current[1];

    rerender();

    const secondRenderColorMode = result.current[1];
    expect(firstRenderColorMode).toBe(secondRenderColorMode);
  });
});

