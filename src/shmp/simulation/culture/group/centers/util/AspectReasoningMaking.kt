package shmp.simulation.culture.group.centers.util

import shmp.random.SampleSpaceObject
import shmp.random.randomElementOrNull
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.random.singleton.testProbability
import shmp.simulation.Controller
import shmp.simulation.culture.group.centers.AspectCenter
import shmp.simulation.culture.group.centers.MemoryCenter
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.IdeationalConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.emptyReasonConversionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.singletonReasonAdditionResult
import shmp.simulation.culture.group.cultureaspect.reasoning.equals
import shmp.simulation.culture.group.request.RequestPool
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.Result
import shmp.simulation.culture.group.request.ResultStatus
import shmp.simulation.space.resource.Resource
import shmp.utils.MovingAverage
import kotlin.math.pow


//class AspectConversion(private val aspectCenter: AspectCenter) : ReasonConversion {
//    override fun makeConversion(complex: ReasonComplex) =
//            takeOutCommonReasonings(memoryCenter)
//}
