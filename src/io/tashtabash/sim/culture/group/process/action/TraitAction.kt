package io.tashtabash.sim.culture.group.process.action

import io.tashtabash.random.singleton.testProbability
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.TraitExtractor


class TestTraitA(group: Group, val extractor: TraitExtractor): AbstractGroupAction(group) {
    override fun run() = extractor
            .extract(group.cultureCenter.traitCenter)
            .testProbability()

    override val internalToString = "Test relation of ${group.name} to $extractor"
}


infix fun TraitExtractor.testOn(group: Group) = TestTraitA(group, this).run()
