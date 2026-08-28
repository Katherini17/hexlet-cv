import { Box, Container, Paper, SimpleGrid, Text } from '@mantine/core'
import { palette } from '@ui/theme/palette'
import { SectionHeading } from './SectionHeading'
import { steps } from '../model/content'

/**
 * «Как это работает» — четыре шага от резюме до торга по офферу.
 *
 * Якорь `how` держит ссылку «Как это работает» из шапки.
 */
export function How() {
  return (
    <Box
      id="how"
      bg={palette.surface}
      style={{
        borderTop: `1px solid ${palette.border}`,
        borderBottom: `1px solid ${palette.border}`,
      }}
    >
      <Container size="lg" px={24} py={64}>
        <SectionHeading
          title="Как это работает"
          text="Четыре шага — и поиск превращается из хаоса в управляемый процесс."
        />
        <SimpleGrid cols={{ base: 1, sm: 2, md: 4 }} spacing={14}>
          {steps.map((step) => (
            <Paper
              key={step.number}
              bg="white"
              radius={14}
              p={22}
              style={{ border: `1px solid ${palette.border}` }}
            >
              <Text ff="monospace" fz={13} fw={700} c={palette.brand} mb={12}>
                {step.number}
              </Text>
              <Text fz={15} fw={800} mb={7}>
                {step.title}
              </Text>
              <Text fz={13} lh={1.6} c={palette.text}>
                {step.text}
              </Text>
            </Paper>
          ))}
        </SimpleGrid>
      </Container>
    </Box>
  )
}
