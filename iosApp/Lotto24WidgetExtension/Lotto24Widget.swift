import WidgetKit
import SwiftUI
import Shared

struct Lotto24WidgetEntry: TimelineEntry {
    let date: Date
    let drawStates: [WidgetDrawState]
}

struct Lotto24WidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> Lotto24WidgetEntry {
        Lotto24WidgetEntry(date: Date(), drawStates: fallbackStates)
    }

    func getSnapshot(
        in context: Context,
        completion: @escaping (Lotto24WidgetEntry) -> Void
    ) {
        Task {
            let states: [WidgetDrawState]

            do {
                let result = try await Lotto24WidgetHelper.shared
                    .getWidgetDrawData(forceRefresh: false)

                states = result.isEmpty ? fallbackStates : result
            } catch {
                states = fallbackStates
            }

            completion(
                Lotto24WidgetEntry(
                    date: Date(),
                    drawStates: states
                )
            )
        }
    }

    func getTimeline(
        in context: Context,
        completion: @escaping (Timeline<Lotto24WidgetEntry>) -> Void
    ) {
        Task {
            let states: [WidgetDrawState]

            do {
                let result = try await Lotto24WidgetHelper.shared
                    .getWidgetDrawData(forceRefresh: false)

                states = result.isEmpty ? fallbackStates : result
            } catch {
                states = fallbackStates
            }

            let entry = Lotto24WidgetEntry(
                date: Date(),
                drawStates: states
            )

            let nextUpdate = Calendar.current.date(
                byAdding: .minute,
                value: 30,
                to: Date()
            ) ?? Date()

            completion(
                Timeline(
                    entries: [entry],
                    policy: .after(nextUpdate)
                )
            )
        }
    }

    private var fallbackStates: [WidgetDrawState] {
        [
            WidgetDrawState(
                lotteryId: "6aus49",
                title: "LOTTO 6aus49",
                formattedDate: "Sa., 21.01.2023",
                nextDrawText: "Nächste Ziehung: Mi., 25.01.2023",
                jackpotAmountValue: "14",
                jackpotUnitValue: "MIO",
                jackpotFormatted: "14 MIO €",
                mainBallsLabel: "GEWINNZAHLEN",
                specialBallLabel: "Superzahl: 7",
                regularNumbers: [4, 19, 24, 43, 44, 45],
                specialNumbers: [7],
                supplementaryGames: [
                    WidgetSupplementaryGame(
                        name: "Spiel 77",
                        winningNumber: "9415812"
                    ),
                    WidgetSupplementaryGame(
                        name: "SUPER6",
                        winningNumber: "381144"
                    )
                ],
                anchorLabel: "6AUS49",
                isFixture: false,
                specialNumberLabel: "Superzahl"
            ),
            WidgetDrawState(
                lotteryId: "eurojackpot",
                title: "Eurojackpot",
                formattedDate: "Fr., 20.01.2023",
                nextDrawText: "Nächste Ziehung: Di., 24.01.2023",
                jackpotAmountValue: "45",
                jackpotUnitValue: "MIO",
                jackpotFormatted: "45 MIO €",
                mainBallsLabel: "5 GEWINNZAHLEN",
                specialBallLabel: "2 EUROZAHLEN",
                regularNumbers: [3, 12, 18, 25, 41],
                specialNumbers: [2, 7],
                supplementaryGames: [],
                anchorLabel: "EURO",
                isFixture: false,
                specialNumberLabel: "Eurozahlen"
            )
        ]
    }
}

struct Lotto24WidgetEntryView: View {
    @Environment(\.widgetFamily) private var family

    let entry: Lotto24WidgetProvider.Entry

    private var isLarge: Bool {
        family == .systemLarge
    }

    var body: some View {
        VStack(spacing: isLarge ? 10 : 6) {
            ForEach(entry.drawStates, id: \.lotteryId) { state in
                LotteryWidgetCard(
                    drawState: state,
                    isLarge: isLarge
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .containerBackground(.clear, for: .widget)
    }
}

// MARK: - AuraTheme Semantic Palette (1:1 Parity with Android)
private enum AuraWidgetPalette {
    static func adaptive(light: Color, dark: Color) -> Color {
        Color(
            UIColor { traits in
                traits.userInterfaceStyle == .dark ? UIColor(dark) : UIColor(light)
            }
        )
    }

    static let surface = adaptive(
        light: Color(hex: 0xFFFFFF),
        dark: Color(hex: 0x0F172A)
    )
    static let surfaceBorder = adaptive(
        light: Color(hex: 0xE2E8F0),
        dark: Color(hex: 0x1E293B)
    )
    static let surfaceContainer = adaptive(
        light: Color(hex: 0xF8FAFC),
        dark: Color(hex: 0x1E293B)
    )
    static let surfaceContainerLow = adaptive(
        light: Color(hex: 0xF1F5F9),
        dark: Color(hex: 0x020617)
    )

    static let onSurface = adaptive(
        light: Color(hex: 0x0F172A),
        dark: Color(hex: 0xF8FAFC)
    )
    static let onSurfaceVariant = adaptive(
        light: Color(hex: 0x475569),
        dark: Color(hex: 0xCBD5E1)
    )
    static let onSurfaceSubtle = adaptive(
        light: Color(hex: 0x64748B),
        dark: Color(hex: 0x94A3B8)
    )
    static let primary = adaptive(
        light: Color(hex: 0xD97706),
        dark: Color(hex: 0xFBBF24)
    )

    static let regularBallBg = adaptive(
        light: Color(hex: 0xFFFFFF),
        dark: Color(hex: 0x1E293B)
    )
    static let regularBallBorder = adaptive(
        light: Color(hex: 0xE2E8F0),
        dark: Color(hex: 0x334155)
    )
}

private struct LotteryWidgetCard: View {
    let drawState: WidgetDrawState
    let isLarge: Bool

    private var isLotto: Bool {
        drawState.lotteryId.localizedCaseInsensitiveContains("6aus49")
    }

    private var anchorWidth: CGFloat {
        isLarge ? 56 : 46
    }

    private var resolvedSpecialBallLabel: String? {
        if let label = drawState.specialBallLabel, !label.trimmingCharacters(in: .whitespaces).isEmpty {
            return label
        }
        guard !drawState.specialNumbers.isEmpty else { return nil }
        if isLotto, let first = drawState.specialNumbers.first {
            return "Superzahl: \(first)"
        }
        return "\(drawState.specialNumbers.count) EUROZAHLEN"
    }

    var body: some View {
        HStack(spacing: 0) {
            anchorView
            contentView
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(AuraWidgetPalette.surface)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 16,
                style: .continuous
            )
        )
        .overlay(
            RoundedRectangle(
                cornerRadius: 16,
                style: .continuous
            )
            .stroke(AuraWidgetPalette.surfaceBorder, lineWidth: 1)
        )
        .shadow(
            color: Color.black.opacity(0.08),
            radius: 4,
            x: 0,
            y: 2
        )
    }

    // ==================== 1. LEFT GOLDEN JACKPOT ANCHOR STRIP ====================
    private var anchorView: some View {
        VStack(spacing: 2) {
            // Top Logo / Monogram (Circle for 6aus49, Rounded 8pt for Eurojackpot)
            VStack(spacing: 2) {
                ZStack {
                    if isLotto {
                        Circle()
                            .fill(Color.white.opacity(0.20))
                            .frame(
                                width: isLarge ? 32 : 24,
                                height: isLarge ? 32 : 24
                            )
                    } else {
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .fill(Color.white.opacity(0.20))
                            .frame(
                                width: isLarge ? 32 : 24,
                                height: isLarge ? 32 : 24
                            )
                    }

                    Text(isLotto ? "🍀" : "E")
                        .font(
                            .system(
                                size: isLarge ? (isLotto ? 15 : 17) : 12,
                                weight: .black
                            )
                        )
                        .foregroundColor(.white)
                }

                Text(isLotto ? "6AUS49" : "EURO")
                    .font(
                        .system(
                            size: isLarge ? 9 : 8,
                            weight: .heavy
                        )
                    )
                    .tracking(0.5)
                    .foregroundColor(Color(hex: 0xFEF3C7))
            }

            Spacer(minLength: 0)

            // Vertical Jackpot Amount
            VStack(spacing: 0) {
                Text(drawState.jackpotAmountValue ?? "–")
                    .font(
                        .system(
                            size: isLarge ? 25 : 18,
                            weight: .black
                        )
                    )
                    .foregroundColor(.white)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)

                Text(drawState.jackpotUnitValue ?? "MIO")
                    .font(
                        .system(
                            size: isLarge ? 13 : 10,
                            weight: .black
                        )
                    )
                    .tracking(0.5)
                    .foregroundColor(Color(hex: 0xFEF3C7))

                Text("EURO")
                    .font(
                        .system(
                            size: isLarge ? 8 : 7,
                            weight: .bold
                        )
                    )
                    .tracking(1.0)
                    .foregroundColor(Color(hex: 0xFDE68A))
            }

            Spacer(minLength: 0)

            // Bottom Seal Mark
            ZStack {
                Circle()
                    .fill(Color.white.opacity(0.15))
                    .frame(
                        width: isLarge ? 20 : 16,
                        height: isLarge ? 20 : 16
                    )

                Text(isLotto ? "✓" : "★")
                    .font(
                        .system(
                            size: isLarge ? 10 : 9,
                            weight: .bold
                        )
                    )
                    .foregroundColor(Color(hex: 0xFEF3C7))
            }
        }
        .frame(width: anchorWidth)
        .frame(maxHeight: .infinity)
        .padding(.vertical, isLarge ? 10 : 6)
        .padding(.horizontal, 4)
        .background(anchorGradient)
    }

    private var anchorGradient: LinearGradient {
        if isLotto {
            return LinearGradient(
                colors: [
                    Color(hex: 0xF59E0B),
                    Color(hex: 0xD97706)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }

        return LinearGradient(
            colors: [
                Color(hex: 0xB45309),
                Color(hex: 0x78350F)
            ],
            startPoint: .top,
            endPoint: .bottom
        )
    }

    // ==================== 2. MAIN CARD BODY ====================
    private var contentView: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Upper Content Container
            VStack(alignment: .leading, spacing: isLarge ? 7 : 4) {
                headerRow
                ballSection
            }
            .padding(.horizontal, isLarge ? 12 : 8)
            .padding(.top, isLarge ? 10 : 6)

            Spacer(minLength: 4)

            // Bottom-Right Next Draw Pill (flush to bottom-trailing corner)
            if let nextDraw = drawState.nextDrawText {
                HStack {
                    Spacer(minLength: 0)
                    nextDrawPill(nextDraw)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(AuraWidgetPalette.surface)
    }

    private var headerRow: some View {
        HStack(alignment: .top, spacing: 6) {
            // Left: Dual-styled Title + Calendar Date
            VStack(alignment: .leading, spacing: 2) {
                if isLotto {
                    HStack(alignment: .firstTextBaseline, spacing: 0) {
                        Text("LOTTO ")
                            .font(
                                .system(
                                    size: isLarge ? 17 : 14,
                                    weight: .black
                                )
                            )
                            .foregroundColor(AuraWidgetPalette.onSurface)

                        Text("6aus49")
                            .font(
                                .system(
                                    size: isLarge ? 13.5 : 11,
                                    weight: .bold
                                )
                            )
                            .foregroundColor(AuraWidgetPalette.primary)
                    }
                    .lineLimit(1)
                } else {
                    Text("Eurojackpot")
                        .font(
                            .system(
                                size: isLarge ? 17 : 14,
                                weight: .black
                            )
                        )
                        .foregroundColor(AuraWidgetPalette.onSurface)
                        .lineLimit(1)
                }

                HStack(spacing: 3) {
                    Text("📅")
                        .font(.system(size: isLarge ? 11 : 9.5))

                    Text(drawState.formattedDate)
                        .font(
                            .system(
                                size: isLarge ? 12.5 : 10.5,
                                weight: .semibold
                            )
                        )
                        .foregroundColor(AuraWidgetPalette.onSurfaceVariant)
                        .lineLimit(1)
                }
            }

            Spacer(minLength: 4)

            // Top-Right Supplementary Games Pill (LOTTO 6aus49 only: Spiel 77, SUPER6)
            if isLotto && !drawState.supplementaryGames.isEmpty {
                supplementaryGamesView
            }
        }
    }

    private var supplementaryGamesView: some View {
        VStack(alignment: .trailing, spacing: 2) {
            ForEach(drawState.supplementaryGames, id: \.name) { game in
                HStack(spacing: 3) {
                    Text("\(game.name):")
                        .font(
                            .system(
                                size: isLarge ? 9.5 : 8,
                                weight: .medium
                            )
                        )
                        .foregroundColor(AuraWidgetPalette.onSurfaceSubtle)

                    Text(game.winningNumber)
                        .font(
                            .system(
                                size: isLarge ? 9.5 : 8,
                                weight: .heavy
                            )
                        )
                        .tracking(0.4)
                        .foregroundColor(
                            game.name.localizedCaseInsensitiveContains("77")
                                ? AuraWidgetPalette.adaptive(
                                    light: Color(hex: 0xD97706),
                                    dark: Color(hex: 0xFBBF24)
                                )
                                : AuraWidgetPalette.adaptive(
                                    light: Color(hex: 0xDC2626),
                                    dark: Color(hex: 0xF87171)
                                )
                        )
                }
                .lineLimit(1)
            }
        }
        .padding(.horizontal, isLarge ? 7 : 5)
        .padding(.vertical, isLarge ? 4 : 2)
        .background(AuraWidgetPalette.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .stroke(AuraWidgetPalette.surfaceBorder, lineWidth: 1)
        )
    }

    // ==================== 3. SUNKEN BALL TRAY ====================
    private var ballSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(drawState.mainBallsLabel)
                    .font(
                        .system(
                            size: isLarge ? 10 : 8.5,
                            weight: .bold
                        )
                    )
                    .tracking(0.5)
                    .foregroundColor(AuraWidgetPalette.onSurfaceSubtle)

                Spacer(minLength: 4)

                if let specialLabel = resolvedSpecialBallLabel {
                    Text(specialLabel)
                        .font(
                            .system(
                                size: isLarge ? 10 : 8.5,
                                weight: .bold
                            )
                        )
                        .tracking(isLotto ? 0 : 0.5)
                        .foregroundColor(
                            isLotto
                                ? AuraWidgetPalette.adaptive(
                                    light: Color(hex: 0xDC2626),
                                    dark: Color(hex: 0xF87171)
                                )
                                : AuraWidgetPalette.adaptive(
                                    light: Color(hex: 0xD97706),
                                    dark: Color(hex: 0xFBBF24)
                                )
                        )
                }
            }

            AdaptiveBallTray(
                regularNumbers: drawState.regularNumbers.map { Int(truncating: $0) },
                specialNumbers: drawState.specialNumbers.map { Int(truncating: $0) },
                isLotto: isLotto,
                isLarge: isLarge
            )
        }
    }

    // ==================== 4. BOTTOM STATUS (NEXT DRAW PILL) ====================
    private func nextDrawPill(_ value: String) -> some View {
        let prefix = "Nächste Ziehung:"
        let hasPrefix = value.lowercased().hasPrefix(prefix.lowercased())
        let datePart = hasPrefix
            ? String(value.dropFirst(prefix.count)).trimmingCharacters(in: .whitespaces)
            : value

        let pillBg = isLotto
            ? Color(hex: 0xCA8A04)
            : Color(hex: 0x451A03)

        let labelColor = isLotto
            ? Color(hex: 0xFEF3C7)
            : Color(hex: 0xFDE68A)

        let dateColor = isLotto
            ? Color.white
            : AuraWidgetPalette.adaptive(
                light: Color(hex: 0xF59E0B),
                dark: Color(hex: 0xFBBF24)
            )

        return HStack(spacing: 0) {
            if hasPrefix {
                Text("Nächste Ziehung: ")
                    .font(
                        .system(
                            size: isLarge ? 11 : 9,
                            weight: .medium
                        )
                    )
                    .foregroundColor(labelColor)
            }

            Text(datePart)
                .font(
                    .system(
                        size: isLarge ? 11 : 9,
                        weight: .bold
                    )
                )
                .foregroundColor(dateColor)
        }
        .lineLimit(1)
        .padding(.horizontal, isLarge ? 12 : 8)
        .padding(.vertical, isLarge ? 4.5 : 3)
        .background(pillBg)
        .clipShape(
            UnevenRoundedRectangle(
                topLeadingRadius: isLarge ? 8 : 6,
                bottomLeadingRadius: 0,
                bottomTrailingRadius: 15,
                topTrailingRadius: 0,
                style: .continuous
            )
        )
    }
}

private struct AdaptiveBallTray: View {
    let regularNumbers: [Int]
    let specialNumbers: [Int]
    let isLotto: Bool
    let isLarge: Bool

    var body: some View {
        GeometryReader { proxy in
            let regularCount = max(regularNumbers.count, 1)
            let specialCount = specialNumbers.count
            let totalBalls = regularCount + specialCount

            let trayHorizontalPadding: CGFloat = 10 // 5pt left + 5pt right
            let podHorizontalPadding: CGFloat = specialCount > 0 ? 6 : 0 // 3pt left + 3pt right
            let podInnerGaps: CGFloat = specialCount > 1 ? CGFloat(specialCount - 1) * 3.5 : 0
            let podSeparatorGap: CGFloat = specialCount > 0 ? (isLarge ? 5 : 4) : 0
            let minRegularGap: CGFloat = isLarge ? 3 : 2
            let regularGapsCount = max(regularCount - 1, 0)

            let usableWidth = max(proxy.size.width - trayHorizontalPadding, 120)
            let fixedReservedWidth = podHorizontalPadding
                + podInnerGaps
                + podSeparatorGap
                + (minRegularGap * CGFloat(regularGapsCount))

            let maxFitDiameter = (usableWidth - fixedReservedWidth) / CGFloat(max(totalBalls, 1))
            let targetDiameter: CGFloat = isLarge ? 28 : 21
            let minDiameter: CGFloat = isLarge ? 20 : 16
            let ballSize = min(targetDiameter, max(minDiameter, maxFitDiameter))

            let specialPodWidth: CGFloat = specialCount > 0
                ? (ballSize * CGFloat(specialCount)) + podInnerGaps + podHorizontalPadding
                : 0
            let remainingForRegular = max(usableWidth - specialPodWidth - podSeparatorGap, 0)
            let remainingForRegularGaps = max(remainingForRegular - (ballSize * CGFloat(regularCount)), 0)
            let interBallGap: CGFloat = regularGapsCount > 0
                ? min(isLarge ? 6 : 4, max(2, remainingForRegularGaps / CGFloat(regularGapsCount)))
                : 0

            HStack(spacing: 0) {
                // Regular Balls
                HStack(spacing: interBallGap) {
                    ForEach(regularNumbers, id: \.self) { number in
                        StaticBall(
                            number: number,
                            backgroundColor: AuraWidgetPalette.regularBallBg,
                            borderColor: AuraWidgetPalette.regularBallBorder,
                            textColor: AuraWidgetPalette.onSurface,
                            size: ballSize
                        )
                    }
                }

                // Special Balls Pod (Inner Contrast Capsule - Superzahl / Eurozahlen)
                if !specialNumbers.isEmpty {
                    Spacer(minLength: podSeparatorGap)

                    let podBg = isLotto
                        ? AuraWidgetPalette.adaptive(
                            light: Color(hex: 0xFEE2E2),
                            dark: Color(hex: 0x450A0A)
                        )
                        : AuraWidgetPalette.adaptive(
                            light: Color(hex: 0xFEF3C7),
                            dark: Color(hex: 0x451A03)
                        )

                    let podBorder = isLotto
                        ? AuraWidgetPalette.adaptive(
                            light: Color(hex: 0xFECACA),
                            dark: Color(hex: 0x7F1D1D)
                        )
                        : AuraWidgetPalette.adaptive(
                            light: Color(hex: 0xFDE68A),
                            dark: Color(hex: 0x78350F)
                        )

                    let specialBallBg = isLotto
                        ? AuraWidgetPalette.adaptive(
                            light: Color(hex: 0xDC2626),
                            dark: Color(hex: 0xEF4444)
                        )
                        : AuraWidgetPalette.adaptive(
                            light: Color(hex: 0xD97706),
                            dark: Color(hex: 0xF59E0B)
                        )

                    HStack(spacing: 3.5) {
                        ForEach(specialNumbers, id: \.self) { number in
                            StaticBall(
                                number: number,
                                backgroundColor: specialBallBg,
                                borderColor: specialBallBg,
                                textColor: .white,
                                size: ballSize
                            )
                        }
                    }
                    .padding(.horizontal, 3)
                    .padding(.vertical, 2)
                    .background(podBg)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule()
                            .stroke(podBorder, lineWidth: 1)
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            .padding(.horizontal, 5)
            .background(AuraWidgetPalette.surfaceContainerLow)
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .stroke(AuraWidgetPalette.surfaceBorder, lineWidth: 1)
            )
        }
        .frame(height: isLarge ? 38 : 28)
    }
}

private struct StaticBall: View {
    let number: Int
    let backgroundColor: Color
    let borderColor: Color
    let textColor: Color
    let size: CGFloat

    var body: some View {
        Text("\(number)")
            .font(
                .system(
                    size: number >= 10 ? size * 0.42 : size * 0.46,
                    weight: .bold
                )
            )
            .foregroundColor(textColor)
            .frame(width: size, height: size)
            .background(backgroundColor)
            .clipShape(Circle())
            .overlay(
                Circle()
                    .stroke(borderColor, lineWidth: 1)
            )
            .shadow(
                color: Color.black.opacity(0.08),
                radius: 1.5,
                y: 1
            )
    }
}

// MARK: - Hex Color Extension
private extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}

@main
struct Lotto24Widget: Widget {
    let kind = "Lotto24Widget"

    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: kind,
            provider: Lotto24WidgetProvider()
        ) { entry in
            Lotto24WidgetEntryView(entry: entry)
        }
        .configurationDisplayName(
            "Lotto24 Ziehungsergebnisse"
        )
        .description(
            "Zeigt LOTTO 6aus49 und Eurojackpot Gewinnzahlen und Jackpots."
        )
        .supportedFamilies([
            .systemLarge
        ])
        .contentMarginsDisabled()
    }
}
