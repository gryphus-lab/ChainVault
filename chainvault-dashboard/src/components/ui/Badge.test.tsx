/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { Badge } from "./Badge";

// Mock the cn utility if you want to test logic,
// or let it run to test final class strings.
describe("Badge Component", () => {
  it("renders the children text correctly", () => {
    render(<Badge>Status Label</Badge>);
    expect(screen.getByText("Status Label")).toBeInTheDocument();
  });

  it("applies the default variant classes when no variant is provided", () => {
    const { container } = render(<Badge>Default</Badge>);
    const badge = container.firstChild as HTMLElement;

    // Checks for base classes and default variant classes
    expect(badge).toHaveClass("bg-gray-100", "text-gray-800", "rounded-full");
  });

  it("applies the correct classes for each variant", () => {
    const variants = [
      { name: "success", expected: "bg-green-100" },
      { name: "warning", expected: "bg-yellow-100" },
      { name: "danger", expected: "bg-red-100" },
    ] as const;

    variants.forEach(({ name, expected }) => {
      const { container } = render(<Badge variant={name}>{name}</Badge>);
      expect(container.firstChild).toHaveClass(expected);
    });
  });

  it("merges custom classNames via the cn utility", () => {
    render(<Badge className="custom-class">Custom</Badge>);
    const badge = screen.getByText("Custom");

    expect(badge).toHaveClass("custom-class");
    // Ensure it still has its base styling
    expect(badge).toHaveClass("inline-flex");
  });

  it("forwards additional HTML attributes (props)", () => {
    render(
      <Badge id="test-badge" data-testid="badge-ui" aria-label="Status">
        Forward
      </Badge>,
    );
    const badge = screen.getByTestId("badge-ui");

    expect(badge).toHaveAttribute("id", "test-badge");
    expect(badge).toHaveAttribute("aria-label", "Status");
  });

  it("renders as a span element", () => {
    const { container } = render(<Badge>Element Check</Badge>);
    expect(container.querySelector("span")).toBeInTheDocument();
  });
});
