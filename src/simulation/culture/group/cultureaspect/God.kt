package simulation.culture.group.cultureaspect

import simulation.culture.thinking.meaning.Meme

class God(
        val aspect: Meme,
        taleSystem: TaleSystem,
        depictSystem: DepictSystem,
        placeSystem: PlaceSystem
) : Worship(taleSystem, depictSystem, placeSystem) {
    override fun toString() = "God of $aspect - ${taleSystem.groupingMeme}"
}