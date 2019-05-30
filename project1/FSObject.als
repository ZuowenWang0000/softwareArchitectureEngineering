//every file system object is parent of one or zero directory
abstract sig FSObject{parent: lone Dir}

sig Dir extends FSObject {contents: set FSObject -Root}

//file is a file system object and it is not parent of any directory
sig File extends FSObject {}

// A directory is the parent of its contents
fact { all d: Dir, o: d.contents | o.parent = d }

// File system is connected
fact { FSObject in Root.*contents }

// The contents path is acyclic
assert acyclic { no d: Dir | d in d.^contents }

// root has no parent
one sig Root extends Dir{}{no parent}

pred show{}
run show for 5
