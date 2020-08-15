package simulation.culture.group.process.action

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.process.TraitExtractor


class TestTrait(group: Group, val extractor: TraitExtractor): AbstractGroupAction(group) {
    override fun run() = testProbability(
            extractor.extract(group.cultureCenter.traitCenter),
            Controller.session.random
    )

    override val internalToString = "Test relation of ${group.name} to $extractor"
}