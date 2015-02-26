import datetime
from bs4 import BeautifulSoup
import urllib2
import MySQLdb
import json
import unirest
import ssl
import ssl
if hasattr(ssl, '_create_unverified_context'):
    ssl._create_default_https_context = ssl._create_unverified_context
    
last_second = -1
url = "http://192.168.43.132/"
time_gap = 5
phone_no = "+919754302349"
myphone = "9566762034"

def getData():
    print "fetching data from mc"
    try:
        page = urllib2.urlopen(url)
    except urllib2.URLError:
        print "error!"
        return "0"
    soup = BeautifulSoup(page)
    value = soup.text[3:7] 
    #pin2 = soup.text[soup.text.rfind("=")+1:soup.text.rfind("=")+4]
    print "value has been fetched "+value
    if(int(value)>=3000):
        print "ACCIDENT!!!!!!!!!!!!!!!"
        urllib2.urlopen("localhost/bike.php?some=2")
    if(int(value)>5 and int(value)<3000):
        print "Overspeed..."
        urllib2.urlopen("localhost/bike.php?some=1")
    return value

def send_sms_try(phone,msg):
    #phone = raw_input("Enter receiver's number: ")
    #msg = raw_input("Enter the message to send: ")
    headers = { "X-Mashape-Authorization": "x6g7JbSWSwmshuXtWsWK9CGdxGlLp1cHaSKjsnLbLpBOW0Y9in" }
    url = "https://160by2.p.mashape.com/index.php?msg="+msg+"&phone="+phone+"&pwd=adityaprasad&uid=9566762034"
    req = urllib2.Request(url, '', headers)
    response = json.loads(urllib2.urlopen(req).read())
    if response['response'] != "done\n":
        print "Error"
    else:
        print "Message sent successfully"

def send_sms(phone,msg):
    print "trying to send sms"
    try:
        response = unirest.get("https://site2sms.p.mashape.com/index.php?msg=hey+dude&phone=9566762034&pwd=adityaprasad&uid=9566762034",headers={"X-Mashape-Key": "x6g7JbSWSwmshuXtWsWK9CGdxGlLp1cHaSKjsnLbLpBOW0Y9in","Accept": "application/json"})
    except urllib2.URLError:
        print "error"
    print "done"
    
def storeData(value):
    # Open database connection
    #db = MySQLdb.connect( "7cfcc050.ngrok.com","root","toor","smarthelmet" )
    db = MySQLdb.connect( "localhost","root","","smartbike" )
    
    # prepare a cursor object using cursor() method
    cursor = db.cursor()
    acc = 0
    # Prepare SQL query to INSERT a record into the database.
    #sql = "INSERT INTO `smartbike`.`master` (`ID`, `Name`, `Accident`) VALUES ('"+str(value1)+"', '"+str(value2)+"', '0');"
    #sql = "INSERT INTO `smartbike`.`a8122514058` (`date`, `time`, `rev`) VALUES (a"+datetime.datetime.strftime(datetime.datetime.now(), '%d%m')+", NOW(), "+str(value)+");"
    sql = "INSERT INTO `smartbike`.`a8122514058` (`date`, `time`, `rev`) VALUES ('a"+datetime.datetime.strftime(datetime.datetime.now(), '%d%m')+"', NOW(), '"+str(value)+"');"
    try:
       # Execute the SQL command
       cursor.execute(sql)
       print "Saved to database"
       # Commit your changes in the database
       db.commit()
    except:
        print "ERROR!!"
        # Rollback in case there is any error
        db.rollback()
        
    # disconnect from server
    db.close()

#send_sms(myphone,"hey")

last_value = getData()

while(True):
    # infinite loop guys! :p
    current_seconds = int(datetime.datetime.strftime(datetime.datetime.now(), '%Y-%m-%d %H:%M:%S')[17:])
    
    if( current_seconds%time_gap == 0 and last_second != current_seconds ):
        #print "time in seconds is "+str(current_seconds)
        data = getData()
        print "we FETCHED THE VALUE AS "+data
        if(data == last_value):
            print "same value, so skipping "
            print "last stored value was "+last_value
        else:
            storeData(str(int(data)-int(last_value)))
            print "stored value "+str(int(data)-int(last_value))+" to database"
            last_value = data
            
        last_second = current_seconds

'''

def storeData(tup):
    value1 = int(tup[0])
    value2 = int(tup[1])
    print "http://7cfcc050.ngrok.com/insert.php?name=8122514058&date="+datetime.datetime.strftime(datetime.datetime.now(), '%d%m')+"&rev="+str(value1)
    urllib2.urlopen("http://7cfcc050.ngrok.com/insert.php?name=8122514058&date="+datetime.datetime.strftime(datetime.datetime.now(), '%d%m')+"&rev="+str(value1))

'''
    
