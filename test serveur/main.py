from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy

app=Flask(__name__)

db = SQLAlchemy()
app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///project.db"
db.init_app(app)
class User(db.Model):
    id=db.Column(db.Integer,primary_key=True)
    nom = db.Column(db.String,unique=True,nullable=False)
    score=db.Column(db.String)
    ami1=db.Column(db.String,unique=True)
    ami2=db.Column(db.String,unique=True)
    ami3=db.Column(db.String,unique=True)
    ami4=db.Column(db.String,unique=True)
    ami5=db.Column(db.String,unique=True)
with app.app_context():
    db.create_all()
    
@app.route("/")
def getAllUsers():
    users = db.session.execute(db.select(User)).all()
    Users=[]
    for user in users:
        utilisateur={"nom":user[0].nom, "score":user[0].score}
        # Users.append({"nom":user[0].nom, "score":user[0].score, "ami1":user[0].ami1,"ami2":user[0].ami2, "ami3":user[0].ami3, "ami4":user[0].ami4, "ami5":user[0].ami5})
        if user[0].ami1:
            score1=User.query.filter_by(nom=user[0].ami1).first()
            utilisateur["ami1"]=user[0].ami1
            utilisateur["ami1Score"]=score1.score
        if user[0].ami2:
            score2=User.query.filter_by(nom=user[0].ami2).first()
            utilisateur["ami2"]=user[0].ami2
            utilisateur["ami2Score"]=score2.score
        if user[0].ami3:
            score3=User.query.filter_by(nom=user[0].ami3).first()
            utilisateur["ami3"]=user[0].ami3
            utilisateur["ami3Score"]=score3.score
        if user[0].ami4:
            score4=User.query.filter_by(nom=user[0].ami4).first()
            utilisateur["ami4"]=user[0].ami4
            utilisateur["ami4Score"]=score4.score
        if user[0].ami5:
            score5=User.query.filter_by(nom=user[0].ami5).first()
            utilisateur["ami5"]=user[0].ami5
            utilisateur["ami5Score"]=score5.score
        Users.append(utilisateur)
    return jsonify(Users)

@app.route("/moi", methods=["GET", "POST"])
def getMe():
    nom = request.form["nom"]
    user = User.query.filter_by(nom=nom).first()
    if user:
        utilisateur = {"nom": user.nom, "score": user.score, "ami1": user.ami1, "ami2": user.ami2, "ami3": user.ami3, "ami4": user.ami4, "ami5": user.ami5}
        if user.ami1:
            score1=User.query.filter_by(nom=user.ami1).first()
            utilisateur["ami1Score"]=score1.score
        if user.ami2:
            score2=User.query.filter_by(nom=user.ami2).first()
            utilisateur["ami2Score"]=score2.score
        if user.ami3:
            score3=User.query.filter_by(nom=user.ami3).first()
            utilisateur["ami3Score"]=score3.score
        if user.ami4:
            score4=User.query.filter_by(nom=user.ami4).first()
            utilisateur["ami4Score"]=score4.score
        if user.ami5:
            score5=User.query.filter_by(nom=user.ami5).first()
            utilisateur["ami5Score"]=score5.score
        # utilisateur = {"nom": user.nom, "score": user.score, "ami1": user.ami1, "ami2": user.ami2, "ami3": user.ami3, "ami4": user.ami4, "ami5": user.ami5}
        return jsonify(utilisateur)
        # return jsonify({"success": True, "message": "Score mis à jour"})
    else:
        return jsonify({"success": False, "message": "Utilisateur non trouvé"}), 404

@app.route("/create", methods=["GET", "POST"])
def createuser():
    pass
    if request.method == "POST":
        user = User(
            nom=request.form["nom"],
            score="0",
            # ami1=request.form["ami1"],
            # ami2=request.form["ami2"],
            # ami3=request.form["ami3"],
            # ami4=request.form["ami4"],
            # ami5=request.form["ami5"],
        )
        db.session.add(user)
        db.session.commit()
        return jsonify({"success": True})
    
@app.route("/setScore", methods=["GET", "POST"])
def setScore():
    nom = request.form["nom"]
    nouveau_score = request.form["score"]
    user = User.query.filter_by(nom=nom).first()
    if user:
        user.score = nouveau_score
        db.session.commit()
        return jsonify({"success": True, "message": "Score mis à jour"})
    else:
        return jsonify({"success": False, "message": "Utilisateur non trouvé"}), 404
    
@app.route("/setAmi", methods=["GET", "POST"])
def setAmi():
    nom = request.form["nom"]
    nouveau_ami_nom = request.form["ami"]
    user = User.query.filter_by(nom=nom).first()
    if user:
        if not user.ami1:
            user.ami1 = nouveau_ami_nom
        elif not user.ami2:
            user.ami2 = nouveau_ami_nom
        elif not user.ami3:
            user.ami3 = nouveau_ami_nom
        elif not user.ami4:
            user.ami4 = nouveau_ami_nom
        elif not user.ami5:
            user.ami5 = nouveau_ami_nom
        db.session.commit()
        return jsonify({"success": True, "message": "Ami ajouté"})
    else:
        return jsonify({"success": False, "message": "Utilisateur non trouvé"}), 404
    
@app.route("/reset", methods=["POST"])
def reset():
    db.session.query(User).delete()
    db.session.commit()
    return jsonify({"success": True, "message": "Toutes les données ont été supprimées"})

app.run(debug=True,port=5001)
# app.run(host="0.0.0.0",port=5001)