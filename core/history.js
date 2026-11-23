export class History{
  constructor(limit=120){this.limit=limit;this.undoStack=[];this.redoStack=[];}
  checkpoint(state){this.undoStack.push(JSON.stringify(state));if(this.undoStack.length>this.limit)this.undoStack.shift();this.redoStack.length=0;}
  undo(current){if(!this.undoStack.length)return null;this.redoStack.push(JSON.stringify(current));return JSON.parse(this.undoStack.pop());}
  redo(current){if(!this.redoStack.length)return null;this.undoStack.push(JSON.stringify(current));return JSON.parse(this.redoStack.pop());}
}
