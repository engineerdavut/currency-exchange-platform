/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'standalone',

  async rewrites() {
    return [
      {
        source: '/api/auth/:path*',
        destination: 'http://nginx:80/api/auth/:path*', // NGINX üzerinden auth servisine yönlendirir
      },
      {
        source: '/api/account/:path*',
        destination: 'http://nginx:80/api/account/:path*', // NGINX üzerinden account servisine yönlendirir
      },
      {
        source: '/api/exchange/:path*',
        destination: 'http://nginx:80/api/exchange/:path*', // NGINX üzerinden exchange servisine yönlendirir
      },
      {
        source: '/api/test/:path*',
        destination: 'http://nginx:80/api/test/:path*', // NGINX üzerinden test servisine yönlendirir
      },
      {
        source: '/api/:path*',
        destination: 'http://localhost:8090/api/:path*'
      },
    ];
  },
};

module.exports = nextConfig;