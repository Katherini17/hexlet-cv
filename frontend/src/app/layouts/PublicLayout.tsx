import { Outlet } from 'react-router'
import { Footer } from '@widgets/footer'
import { Header } from '@widgets/header'
import { useScrollToHash } from '@shared/hooks/useScrollToHash'

export function PublicLayout() {
  useScrollToHash()

  return (
    <>
      <Header />
      <Outlet />
      <Footer />
    </>
  )
}
