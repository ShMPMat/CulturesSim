package shmp.simulation.culture.group.cultureaspect.util

import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.cultureaspect.*
import shmp.generator.culture.worldview.reasoning.EqualityReasoning
import shmp.generator.culture.worldview.reasoning.ReasonField
import shmp.generator.culture.worldview.reasoning.concept.ObjectConcept
import shmp.simulation.culture.group.cultureaspect.worship.ConceptObjectWorship
import shmp.simulation.culture.group.cultureaspect.worship.GodWorship
import shmp.simulation.culture.group.cultureaspect.worship.MultigroupWorshipWrapper
import shmp.simulation.culture.group.cultureaspect.worship.Worship
import shmp.generator.culture.worldview.Meme


fun takeOutSimilarRituals(aspectPool: MutableCultureAspectPool, bound: Int = 3): RitualSystem? {
    val (popularReason, popularRituals) = aspectPool
            .filter { it is Ritual }
            .map { it as Ritual }
            .groupBy { it.reason }
            .maxByOrNull { it.value.size }
            ?: return null
    if (popularRituals.size >= bound) {
        aspectPool.removeAll(popularRituals)
        return RitualSystem(popularRituals, popularReason)
    }
    return null
}

fun takeOutSimilarTales(aspectPool: MutableCultureAspectPool, bound: Int = 3): TaleSystem? {
    val (popularConcept, popularTales) = aspectPool
            .filter { it is Tale }
            .map { it as Tale }
            .groupBy { it.info.actorConcept }
            .maxByOrNull { it.value.size }
            ?: return null

    if (popularTales.size >= bound) {
        aspectPool.removeAll(popularTales)
        return TaleSystem(popularTales, popularConcept);
    }
    return null
}

fun takeOutWorship(reasonField: ReasonField, aspectPool: MutableCultureAspectPool): Worship? {
    return takeOutConceptWorship(reasonField, aspectPool)
}

private fun takeOutConceptWorship(reasonField: ReasonField, aspectPool: MutableCultureAspectPool): Worship? {
    val reasoning = reasonField.commonReasonings.reasonings
            .filterIsInstance<EqualityReasoning>()
            .filter { it.objectConcept is ObjectConcept }
            .randomElementOrNull()
            ?: return null
    val concept: ObjectConcept = reasoning.objectConcept as ObjectConcept

    val existing = aspectPool.worships
            .filterIsInstance<ConceptObjectWorship>()
            .map { it.objectConcept }
            .toSet()

    val taleSystem = aspectPool
            .filter { it is TaleSystem }
            .map { it as TaleSystem }
            .filter { it.groupingConcept !in existing }
            .filter { it.groupingConcept == concept }
            .randomElementOrNull()
            ?: return null

    val depictions = aspectPool.all
            .filterIsInstance<DepictObject>()
            .filter { it.objectConcept == concept}
    aspectPool.removeAll(depictions)
    val depictSystem = DepictSystem(depictions, concept.meme, concept)

    return MultigroupWorshipWrapper(Worship(
            ConceptObjectWorship(concept),
            taleSystem,
            depictSystem,
            PlaceSystem(mutableSetOf(), true),
            reasonField.commonReasonings.extractComplexFor(concept, "Worship of $concept"),
            mutableListOf()
    ))
}

fun takeOutDepictionSystem(aspectPool: MutableCultureAspectPool, groupingMeme: Meme, bound: Int = 3): DepictSystem? {
    val depictions = aspectPool
            .filter { it is DepictObject }
            .map { it as DepictObject }
            .filter { d -> d.meme.anyMatch { it.topMemeCopy() == groupingMeme } }
    if (depictions.size >= bound) {
        aspectPool.removeAll(depictions)
        return DepictSystem(depictions, groupingMeme, null)
    }
    return null
}

fun takeOutGod(aspectPool: MutableCultureAspectPool, group: Group): CultureAspect? {
    val chosen = aspectPool.worships
            .filter { it.worshipObject is ConceptObjectWorship }
            .randomElementOrNull()
            ?: return null

    val meme = chosen.worshipObject.name

    val sphere = chosen.reasonComplex.reasonings
            .filterIsInstance<EqualityReasoning>()
            .randomElementOrNull()
            ?.subjectConcept
            ?: return null

    val god = GodWorship(meme, sphere)

    aspectPool.remove(chosen)

    return chosen.swapWorship(god)
}
