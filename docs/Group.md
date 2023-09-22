# Group

## Structure

Group consists of centers:

- `ProcessCenter` - handle behaviors;
- `CultureCenter` - groups other culture-related centers:
  - `TraitCenter`
  - `MemoryCenter`
  - `CultureAspectCenter`
  - `AspectCenter`
  - `RequestCenter`
- TODO

## Behaviour classes

- `GroupBehaviour` - base class which is added to `ProcessCenter` to run some logic every turn;
- `GroupPseudoAction` - TODO
  - `GroupAction` - encapsulates logic which requires only one `Group`, run inside `Behaviours`;
- `GroupInteraction` - encapsulates interaction logic between 2 `Groups`, run inside `Behaviours`;
  - Returns `ProcessResult` which can be propagated out of wrapping `Behaviour`;
  - Also internally handles `ProcessResult` for the other `Group` and makes the `Group` consume it.

## Culture Aspect Classes

- `CultureAspect` - core class;
  - TODO
- `WorshipObjectDependent` - additional int for classes which have WorshipObject and can swap it;
  - Not necessarily `CultureAspects`;
  - `WorshipFeature` - stored inside `Worship`. 