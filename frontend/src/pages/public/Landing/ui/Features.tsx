import { Box, Container, Group, Paper, SimpleGrid, Text } from '@mantine/core'
import {
  IconBriefcase,
  IconFileText,
  IconLayoutKanban,
  IconMessage,
} from '@tabler/icons-react'
import { palette } from '@ui/theme/palette'
import { SectionHeading } from './SectionHeading'
import { features, type Feature } from '../model/content'

const icons: Record<Feature['id'], typeof IconBriefcase> = {
  vacancies: IconBriefcase,
  resume: IconFileText,
  tracker: IconLayoutKanban,
  interview: IconMessage,
}

/** Карточка возможности. Тёмный вариант — тот, у которого есть бейдж. */
function FeatureCard({ feature }: { feature: Feature }) {
  const Icon = icons[feature.id]
  const dark = Boolean(feature.badge)

  return (
    <Paper
      bg={dark ? palette.ink : 'white'}
      c={dark ? 'white' : undefined}
      radius={14}
      px={26}
      py={24}
      style={dark ? undefined : { border: `1px solid ${palette.border}` }}
    >
      <Group gap={8} mb={14}>
        <Box
          w={38}
          h={38}
          bg={dark ? palette.aiSoft : palette.brandSoft}
          style={{
            borderRadius: 10,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Icon
            size={18}
            stroke={1.9}
            color={dark ? palette.aiText : palette.brand}
          />
        </Box>
        {feature.badge && (
          <Text
            fz={10}
            fw={800}
            tt="uppercase"
            c={palette.aiText}
            bg={palette.aiSoft}
            px={8}
            py={3}
            style={{ borderRadius: 99, letterSpacing: '0.6px' }}
          >
            {feature.badge}
          </Text>
        )}
      </Group>
      <Text fz={16.5} fw={800} mb={7}>
        {feature.title}
      </Text>
      <Text fz={13.5} lh={1.6} c={dark ? palette.onInk : palette.text}>
        {feature.text}
      </Text>
    </Paper>
  )
}

/**
 * Возможности сервиса — сетка 2×2.
 *
 * Якорь `features` держит ссылку «Возможности» из шапки, менять его нельзя
 * без правки конфигурации навигации.
 */
export function Features() {
  return (
    <Box
      id="features"
      bg={palette.surface}
      style={{
        borderTop: `1px solid ${palette.border}`,
        borderBottom: `1px solid ${palette.border}`,
      }}
    >
      <Container size="lg" px={24} py={64}>
        <SectionHeading
          title="Всё, что между «ищу работу» и «вышел на работу»"
          text="Один контекст на все шаги: вакансия знает про ваше резюме, письмо — про вакансию, тренировка — про завтрашний собес."
        />
        <SimpleGrid cols={{ base: 1, sm: 2 }} spacing={14}>
          {features.map((feature) => (
            <FeatureCard key={feature.id} feature={feature} />
          ))}
        </SimpleGrid>
      </Container>
    </Box>
  )
}
