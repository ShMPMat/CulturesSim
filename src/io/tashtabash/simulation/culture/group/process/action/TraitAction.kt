package io.tashtabash.simulation.culture.group.process.action

import io.tashtabash.random.singleton.testProbability
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.process.TraitExtractor


class TestTraitA(group: Group, val extractor: TraitExtractor): AbstractGroupAction(group) {
    override fun run() = extractor
            .extract(group.cultureCenter.traitCenter)
            .testProbability()

    override val internalToString = "Test relation of ${group.name} to $extractor"
}
