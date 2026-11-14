import WidgetKit
import SwiftUI
import Shared

/**
 * Timeline entry wrapping the shared [WidgetDrawState] list from the KMP core.
 */
struct Lotto24WidgetEntry: TimelineEntry {
    let date: Date
    let drawStates: [WidgetDrawState]
}

/**
 * Timeline provider for Lotto24 WidgetKit.
 *
 * Resolves live, cached, or fallback draw results from the shared KMP core
 * via [Lotto24WidgetHelper], ensuring full parity with Android Jetpack Glance.
 */
struct Lotto24WidgetProvider: TimelineProvider {

    func placeholder(in context: Context) -> Lotto24WidgetEntry {
        Lotto24WidgetEntry(date: Date(), drawStates: fallbackStates)
    }

    func getSnapshot(in context: Context, completion: @escaping (Lotto24WidgetEntry) -> Void) {
        Task {
            let states: [WidgetDrawState]
            do {
                let result = try await Lotto24WidgetHelper.shared.getWidgetDrawData(forceRefresh: false)
                states = result.isEmpty ? fallbackStates : result
            } catch {
                states = fallbackStates
            }
            completion(Lotto24WidgetEntry(date: Date(), drawStates: states))
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<Lotto24WidgetEntry>) -> Void) {
        Task {
            let states: [WidgetDrawState]
            do {
                let result = try await Lotto24WidgetHelper.shared.getWidgetDrawData(forceRefresh: false)
                states = result.isEmpty ? fallbackStates : result
            } catch {
                states = fallbackStates
            }

            // Periodic refresh every 30 minutes
            let nextUpdate = Calendar.current.date(byAdding: .minute, value: 30, to: Date()) ?? Date()
            let entry = Lotto24WidgetEntry(date: Date(), drawStates: states)
            let timeline = Timeline(entries: [entry], policy: .after(nextUpdate))
            completion(timeline)
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
                    WidgetSupplementaryGame(name: "Spiel 77", winningNumber: "9415812"),
                    WidgetSupplementaryGame(name: "SUPER6", winningNumber: "381144")
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

/**
 * SwiftUI View representing the in-app Aura Lotto dual card design without animations.
 * Adaptively renders large full cards in `.systemLarge` and a compact layout in `.systemMedium`.
 */
struct Lotto24WidgetEntryView: View {
    @Environment(\.widgetFamily) var family
    var entry: Lotto24WidgetProvider.Entry

    var body: some View {
        VStack(spacing: family == .systemLarge ? 8 : 4) {
            ForEach(entry.drawStates, id: \.lotteryId) { drawState in
                HomeStyleCardView(drawState: drawState, isLarge: family == .systemLarge)
            }
        }
        .padding(family == .systemLarge ? 8 : 4)
        .background(Color(red: 0.95, green: 0.96, blue: 0.98)) // App canvas (#F1F5F9)
    }
}

/**
 * Matches the home app screen's [LotteryCard] design in SwiftUI:
 * - Left vertical Golden Anchor Strip with Monogram, Anchor Label, Jackpot & Bottom Seal
 * - Main Card Body with Title, Date, Supplementary Games, Sunken Ball Tray & Next Draw Footer
 */
private struct HomeStyleCardView: View {
    let drawState: WidgetDrawState
    let isLarge: Bool

    private var isLotto6aus49: Bool {
        drawState.lotteryId.localizedCaseInsensitiveContains("6aus49")
    }

    private var anchorGradient: LinearGradient {
        if isLotto6aus49 {
            return LinearGradient(
                colors: [Color(red: 0.90, green: 0.66, blue: 0.12), Color(red: 0.78, green: 0.45, blue: 0.05)],
                startPoint: .top,
                endPoint: .bottom
            )
        } else {
            return LinearGradient(
                colors: [Color(red: 0.85, green: 0.47, blue: 0.02), Color(red: 0.65, green: 0.30, blue: 0.02)],
                startPoint: .top,
                endPoint: .bottom
            )
        }
    }

    var body: some View {
        HStack(spacing: 0) {
            // ==================== 1. LEFT GOLDEN ANCHOR STRIP ====================
            VStack(spacing: 2) {
                // Top Monogram
                ZStack {
                    Circle()
                        .fill(Color.black.opacity(0.18))
                        .frame(width: isLarge ? 22 : 18, height: isLarge ? 22 : 18)
                    Text(isLotto6aus49 ? "🍀" : "E")
                        .font(.system(size: isLarge ? 11 : 9, weight: .bold))
                        .foregroundColor(.white)
                }

                Text(drawState.anchorLabel)
                    .font(.system(size: isLarge ? 7 : 6, weight: .heavy))
                    .foregroundColor(Color(red: 0.99, green: 0.95, blue: 0.78))

                Spacer(minLength: 1)

                // Jackpot Value
                Text(drawState.jackpotAmountValue ?? "–")
                    .font(.system(size: isLarge ? 17 : 14, weight: .black))
                    .foregroundColor(.white)

                Text(drawState.jackpotUnitValue ?? "MIO")
                    .font(.system(size: isLarge ? 9 : 7, weight: .bold))
                    .foregroundColor(Color(red: 0.99, green: 0.95, blue: 0.78))

                Text("EURO")
                    .font(.system(size: isLarge ? 7 : 5, weight: .medium))
                    .foregroundColor(Color(red: 0.99, green: 0.90, blue: 0.54))

                Spacer(minLength: 1)

                // Bottom Seal
                ZStack {
                    Circle()
                        .fill(Color.black.opacity(0.14))
                        .frame(width: isLarge ? 16 : 13, height: isLarge ? 16 : 13)
                    Text(isLotto6aus49 ? "✓" : "★")
                        .font(.system(size: isLarge ? 9 : 7, weight: .bold))
                        .foregroundColor(Color(red: 0.99, green: 0.95, blue: 0.78))
                }
            }
            .frame(width: isLarge ? 50 : 42)
            .padding(.vertical, isLarge ? 6 : 4)
            .background(anchorGradient)

            // ==================== 2. MAIN CARD BODY ====================
            VStack(alignment: .leading, spacing: isLarge ? 3 : 2) {
                // Header: Title & Date
                HStack {
                    Text(drawState.title)
                        .font(.system(size: isLarge ? 12 : 10, weight: .bold))
                        .foregroundColor(Color(red: 0.06, green: 0.09, blue: 0.16))

                    Spacer()

                    Text("📅 \(drawState.formattedDate)")
                        .font(.system(size: isLarge ? 8 : 7, weight: .medium))
                        .foregroundColor(Color(red: 0.39, green: 0.45, blue: 0.55))
                }

                // Supplementary Games (LOTTO 6aus49 only, visible in Large Widget)
                if isLarge && !drawState.supplementaryGames.isEmpty {
                    HStack(spacing: 6) {
                        ForEach(drawState.supplementaryGames, id: \.name) { game in
                            HStack(spacing: 2) {
                                Text("\(game.name):")
                                    .font(.system(size: 8, weight: .medium))
                                    .foregroundColor(Color(red: 0.39, green: 0.45, blue: 0.55))
                                Text(game.winningNumber)
                                    .font(.system(size: 8, weight: .black))
                                    .foregroundColor(game.name.localizedCaseInsensitiveContains("77") ? Color(red: 0.85, green: 0.47, blue: 0.02) : Color(red: 0.86, green: 0.15, blue: 0.15))
                            }
                        }
                    }
                }

                // Tray Header Labels
                HStack {
                    Text(drawState.mainBallsLabel)
                        .font(.system(size: isLarge ? 7 : 6, weight: .bold))
                        .foregroundColor(Color(red: 0.58, green: 0.64, blue: 0.72))

                    Spacer()

                    if let specialLabel = drawState.specialBallLabel {
                        Text(specialLabel)
                            .font(.system(size: isLarge ? 7 : 6, weight: .bold))
                            .foregroundColor(isLotto6aus49 ? Color(red: 0.86, green: 0.15, blue: 0.15) : Color(red: 0.85, green: 0.47, blue: 0.02))
                    }
                }

                // Sunken Ball Tray
                HStack(spacing: 3) {
                    // Regular Balls
                    ForEach(drawState.regularNumbers.map { Int(truncating: $0) }, id: \.self) { num in
                        StaticBallView(number: num, backgroundColor: .white, textColor: Color(red: 0.06, green: 0.09, blue: 0.16), size: isLarge ? 20 : 17)
                    }

                    Spacer(minLength: 2)

                    // Special Pod
                    HStack(spacing: 2) {
                        ForEach(drawState.specialNumbers.map { Int(truncating: $0) }, id: \.self) { specialNum in
                            StaticBallView(
                                number: specialNum,
                                backgroundColor: isLotto6aus49 ? Color(red: 0.86, green: 0.15, blue: 0.15) : Color(red: 0.85, green: 0.47, blue: 0.02),
                                textColor: .white,
                                size: isLarge ? 20 : 17
                            )
                        }
                    }
                    .padding(.horizontal, 3)
                    .padding(.vertical, 2)
                    .background(isLotto6aus49 ? Color(red: 1.0, green: 0.89, blue: 0.89) : Color(red: 0.99, green: 0.95, blue: 0.78))
                    .cornerRadius(999)
                }
                .padding(.horizontal, 4)
                .padding(.vertical, isLarge ? 3 : 2)
                .background(Color(red: 0.97, green: 0.98, blue: 0.99))
                .cornerRadius(999)

                // Next Draw Section (in Large Widget)
                if isLarge, let nextDraw = drawState.nextDrawText {
                    HStack {
                        Spacer()
                        Text(nextDraw)
                            .font(.system(size: 8, weight: .bold))
                            .foregroundColor(Color(red: 0.11, green: 0.31, blue: 0.85))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(red: 0.93, green: 0.95, blue: 1.0))
                            .cornerRadius(4)
                    }
                }
            }
            .padding(.horizontal, isLarge ? 8 : 6)
            .padding(.vertical, isLarge ? 6 : 4)
            .background(Color.white)
        }
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.06), radius: 3, x: 0, y: 1)
    }
}

/**
 * Static non-animated spherical ball view.
 */
private struct StaticBallView: View {
    let number: Int
    let backgroundColor: Color
    let textColor: Color
    var size: CGFloat = 20

    var body: some View {
        Text("\(number)")
            .font(.system(size: number >= 10 ? (size > 18 ? 8 : 7) : (size > 18 ? 9 : 8), weight: .bold))
            .foregroundColor(textColor)
            .frame(width: size, height: size)
            .background(backgroundColor)
            .clipShape(Circle())
            .overlay(Circle().stroke(Color.black.opacity(0.08), lineWidth: 0.5))
    }
}

/**
 * iOS Widget declaration for WidgetKit.
 */
@main
struct Lotto24Widget: Widget {
    let kind: String = "Lotto24Widget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Lotto24WidgetProvider()) { entry in
            Lotto24WidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Lotto24 Ziehungsergebnisse")
        .description("Zeigt LOTTO 6aus49 und Eurojackpot Gewinnzahlen und Jackpots.")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}
