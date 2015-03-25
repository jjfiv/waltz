# Waltz

A tool for low-level information retrieval research.

## Non-Goals

- To be a useful search engine that you can drag and drop into another project.
- To be the fastest implementation
- To have a web frontend
- To provide tokenization (of any kind)
- To provide parsing (of any formats)
- To provide stemming
- To provide stopping or stopword lists
- To provide a query language
- To implement all the algorithms
- To include protocol buffers, thrift formats, tupleflow-typebuilder objects, or any other wacky serialization by default.

## Goals

- To enable storing and querying of your weird, wacky data in your weird, wacky format.
- To be as minimally useful as possible.
- To not care where you store your data, only kind of how.
- To prefer flexibility: Interfaces > Abstract Classes > Classes
- To be slightly compatible with [Galago](http://lemurproject.org/galago.php)
- To enable changing of everything.
- To have tests - unit tests - for all core functionality.
- To eventually move all dependency on Galago's utility functions out to a galago-storage-compat and have galago-compat become galago-scoring-compat

