import {
  Box,
  Button,
  Container,
  Paper,
  SimpleGrid,
  Stack,
  Text,
} from '@mantine/core'
import { Link } from 'react-router'
import { palette, shadows } from '@ui/theme/palette'
import { SectionHeading } from './SectionHeading'
import { plans, type Plan } from '../model/content'

/** Карточка тарифа. Кнопка прижата к низу, чтобы карточки выровнялись. */
function PlanCard({ plan }: { plan: Plan }) {
  const dark = plan.accent === 'dark'
  const highlighted = plan.accent === 'brand'

  return (
    <Paper
      pos="relative"
      bg={dark ? palette.ink : 'white'}
      c={dark ? 'white' : undefined}
      radius={16}
      px={28}
      py={26}
      h="100%"
      style={{
        display: 'flex',
        flexDirection: 'column',
        border: dark
          ? undefined
          : highlighted
            ? `2px solid ${palette.brand}`
            : `1px solid ${palette.border}`,
        boxShadow: highlighted ? shadows.brand : undefined,
      }}
    >
      {plan.badge && (
        <Text
          pos="absolute"
          top={-11}
          left={28}
          fz={10.5}
          fw={800}
          tt="uppercase"
          c="white"
          bg={palette.brand}
          px={11}
          py={3}
          style={{ borderRadius: 99, letterSpacing: '0.6px' }}
        >
          {plan.badge}
        </Text>
      )}

      <Text fz={14} fw={800} mb={6}>
        {plan.name}
      </Text>
      <Text fz={30} fw={800} mb={16} style={{ letterSpacing: '-0.8px' }}>
        {plan.price}{' '}
        <Text span fz={13} fw={600} c={dark ? '#8B909E' : palette.muted}>
          {plan.priceNote}
        </Text>
      </Text>

      <Stack gap={9} mb={22} fz={13} c={dark ? palette.onInkBright : palette.inkSoft}>
        {plan.items.map((item) => (
          <Text key={item} fz={13} lh={1.5} inherit>
            · {item}
          </Text>
        ))}
      </Stack>

      <Button
        component={Link}
        to={plan.to}
        mt="auto"
        h={42}
        radius={9}
        fullWidth
        variant={highlighted ? 'filled' : 'default'}
        // На тёмной карточке кнопка полупрозрачная: сплошная белая перетянула
        // бы внимание с «Про», который в прототипе выделен намеренно.
        style={
          dark
            ? {
                background: 'rgba(255, 255, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.25)',
                color: 'white',
              }
            : undefined
        }
      >
        {plan.action}
      </Button>
    </Paper>
  )
}

/**
 * Тарифы: три плана, средний выделен рамкой, третий — тёмный, для студентов.
 *
 * Якорь `pricing` держит ссылку «Тарифы» из шапки.
 */
export function Pricing() {
  return (
    <Container id="pricing" size="lg" px={24} py={64}>
      <SectionHeading
        title="Тарифы"
        text="Начните бесплатно. «Про» нужен, когда поиск становится активным."
      />
      <SimpleGrid cols={{ base: 1, sm: 3 }} spacing={14}>
        {plans.map((plan) => (
          <PlanCard key={plan.id} plan={plan} />
        ))}
      </SimpleGrid>
      <Box mt={14}>
        <Text fz={12} c={palette.faint}>
          Цены прототипа — для обсуждения модели монетизации.
        </Text>
      </Box>
    </Container>
  )
}
