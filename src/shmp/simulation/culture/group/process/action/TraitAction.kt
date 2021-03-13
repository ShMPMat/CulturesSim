package shmp.simulation.culture.group.process.action

import shmp.random.singleton.testProbability
import shmp.random.testProbability
import shmp.simulation.Controller
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.process.TraitExtractor


class TestTraitA(group: Group, val extractor: TraitExtractor): AbstractGroupAction(group) {
    override fun run() = extractor
            .extract(group.cultureCenter.traitCenter)
            .testProbability()

    override val internalToString = "Test relation of ${group.name} to $extractor"
}
