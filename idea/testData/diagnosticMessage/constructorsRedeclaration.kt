// !DIAGNOSTICS_NUMBER: 2
// !DIAGNOSTICS: CONFLICTING_OVERLOADS
// !MESSAGE_TYPE: TEXT
class Element() {
    constructor(x : String) : this() {}
    constructor(x : String) : this() {}
}