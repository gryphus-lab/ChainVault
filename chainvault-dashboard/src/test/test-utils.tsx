/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactElement, ReactNode } from "react";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      staleTime: Infinity,
    },
  },
});

const AllTheProviders = ({ children }: { children: ReactNode }) => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{children}</BrowserRouter>
    </QueryClientProvider>
  );
};

export const customRender = (ui: ReactElement) => {
  return render(ui, { wrapper: AllTheProviders });
};

// Re-export everything from testing-library
export * from "@testing-library/react";
export { customRender as render };
