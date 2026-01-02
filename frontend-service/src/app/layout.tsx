import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'WHO Cloud Platform',
  description: 'Enterprise Microservices Platform with OAuth 2.0',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
