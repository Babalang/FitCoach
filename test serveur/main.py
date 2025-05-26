from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy

app=Flask(__name__)

db = SQLAlchemy()
app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///project.db"
db.init_app(app)
# with app.app_context():
#     db.create_all()
class Note(db.Model):
    id=db.Column(db.Integer,primary_key=True)
    title = db.Column(db.String,unique=True,nullable=False)
    description=db.Column(db.String)
    
@app.route("/")
def getAllNotes():
    notes = db.session.execute(db.select(Note)).all()
    myNotes=[]
    for note in notes:
        myNotes.append({"title":note[0].title, "description":note[0].description})
    return jsonify(myNotes)

@app.route("/create", methods=["GET", "POST"])
def createnote():
    pass
    if request.method == "POST":
        note = Note(
            title=request.form["title"],
            description=request.form["description"],
        )
        db.session.add(note)
        db.session.commit()
        return jsonify({"success": True})

app.run(debug=True,port=5001)
# app.run(host="0.0.0.0",port=5001)