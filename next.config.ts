import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  poweredByHeader: false,
  experimental: {
    serverExternalPackages: ["ccxt"],
  },
};

export default nextConfig;
