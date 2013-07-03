db = db.getSiblingDB('admin')
if ( db.system.users.find({user: "admin"}).count()==0 ) 
{ 
  db.addUser({user:"admin", pwd:"jabberw0cky", roles: ["userAdminAnyDatabase"]}) 
}
