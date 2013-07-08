db = db.getSiblingDB('admin')
if ( db.system.users.find({user: "admin"}).count()==0 ) 
{ 
  db.addUser('admin', 'jabberw0cky', 'userAdminAnyDatabase') 
}
