package shmp.simulation.culture.group.process.action

import shmp.random.testProbability
import shmp.simulation.Controller
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.process.TraitExtractor


class TestTraitA(group: Group, val extractor: TraitExtractor): AbstractGroupAction(group) {
    override fun run() = testProbability(
            extractor.extract(group.cultureCenter.traitCenter),
            Controller.session.random
    )

    override val internalToString = "Test relation of ${group.name} to $extractor"
}
