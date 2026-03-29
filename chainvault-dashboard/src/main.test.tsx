/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach } from "vitest";

// 1. Mock the external modules BEFORE importing main
vi.mock("react-dom/client", () => ({
  default: {
    createRoot: vi.fn().mockReturnValue({
      render: vi.fn(),
    }),
  },
}));

vi.mock("./App", () => ({
  default: () => <div data-testid="app-component" />,
}));

describe("Application Entry Point", () => {
  beforeEach(() => {
    // Reset mocks and prepare DOM
    vi.clearAllMocks();
    document.body.innerHTML = '<div id="root"></div>';
  });

  it("should render the App component inside providers", async () => {
    const ReactDOM = (await import("react-dom/client")).default;

    // Import main.tsx to trigger the execution
    await import("./main");

    // Verify createRoot was called with the #root element
    expect(ReactDOM.createRoot).toHaveBeenCalledWith(
      document.getElementById("root"),
    );

    // Verify render was called
    const rootInstance = vi.mocked(ReactDOM.createRoot).mock.results[0].value;
    expect(rootInstance.render).toHaveBeenCalled();
  });
});
