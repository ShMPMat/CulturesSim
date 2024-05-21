# Group

## Structure

Group consists of centers:

- `ProcessCenter` - handle behaviors;
- `CultureCenter` - Group's other culture-related centers:
  - `TraitCenter` - general character traits for a Group as a whole;
  - `MemoryCenter`
  - `CultureAspectCenter` - stores CultureAspects;
  - `AspectCenter` - stores Aspects;
  - `RequestCenter` - handles Requests;
- TODO

## Behaviour classes

- `GroupAction`: helper class evaluating some value for a group;
- `GroupBehaviour`: base class which is added to `ProcessCenter` to run some logic every turn;
- `GroupPseudoAction`: TODO
  - `GroupAction`: encapsulates logic which requires only one `Group`, run inside `Behaviours`;
- `GroupInteraction`: encapsulates interaction logic between 2 `Groups`, run inside `Behaviours`;
  - Returns `ProcessResult` which can be propagated out of wrapping `Behaviour`;
  - Internally handles `ProcessResult` for the other `Group` and makes the `Group` consumeit.

## Culture Aspect Classes

- `CultureAspect` - core class;
  - TODO
- `WorshipObjectDependent` - additional int for classes which have WorshipObject and can swap it;
  - Not necessarily `CultureAspects`;
  - `WorshipFeature` - stored inside `Worship`. 
