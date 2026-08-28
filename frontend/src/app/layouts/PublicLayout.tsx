import { Outlet } from 'react-router'
import { Footer } from '@widgets/footer'
import { Header } from '@widgets/header'
import { PromoBanner } from '@widgets/promo-banner'
import { useScrollToHash } from '@shared/hooks/useScrollToHash'

export function PublicLayout() {
  useScrollToHash()

  return (
    <>
      <PromoBanner />
      <Header />
      <Outlet />
      <Footer />
    </>
  )
}
