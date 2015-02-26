import datetime
from bs4 import BeautifulSoup
import urllib2
import MySQLdb

last_second = -1
url = "http://192.168.1.101/"
time_gap = 3

def getData():
    print "fetching data from mc"
    page = urllib2.urlopen(url)
    soup = BeautifulSoup(page)
    value = soup.text[3:6] 
    #pin2 = soup.text[soup.text.rfind("=")+1:soup.text.rfind("=")+4]
    print "value has been fetched "+value
    return value


def storeData(value):
    # Open database connection
    #db = MySQLdb.connect( "7cfcc050.ngrok.com","root","toor","smarthelmet" )
    db = MySQLdb.connect( "localhost","root","","smartbike" )
    
    # prepare a cursor object using cursor() method
    cursor = db.cursor()
    acc = 0
    if(int(value)>3000):
        acc = 1
        
    # Prepare SQL query to INSERT a record into the database.
    #sql = "INSERT INTO `smartbike`.`master` (`ID`, `Name`, `Accident`) VALUES ('"+str(value1)+"', '"+str(value2)+"', '0');"
    sql = "INSERT INTO `smartbike`.`a8122514058` (`date`, `time`, `rev`) VALUES (a"+datetime.datetime.strftime(datetime.datetime.now(), '%d%m')+", NOW(), "+str(value)+");"
    #sql = "INSERT INTO `smartbike`.`master` (`ID`, `Name`, `Accident`) VALUES ("+datetime.datetime.strftime(datetime.datetime.now(), '%d%m%y')+", "+str(value)+", "+str(acc)+");"

    try:
       # Execute the SQL command
       cursor.execute(sql)
       print "Saved to database"
       # Commit your changes in the database
       db.commit()
    except:
       # Rollback in case there is any error
       db.rollback()
       
    # disconnect from server
    db.close()


while(True):
    # infinite loop guys! :p
    current_seconds = int(datetime.datetime.strftime(datetime.datetime.now(), '%Y-%m-%d %H:%M:%S')[17:])
    
    if( current_seconds%time_gap == 0 and last_second != current_seconds ):
        print "time in seconds is "+str(current_seconds)
        data = getData()
        storeData(data)
        last_second = current_seconds

'''

def storeData(tup):
    value1 = int(tup[0])
    value2 = int(tup[1])
    print "http://7cfcc050.ngrok.com/insert.php?name=8122514058&date="+datetime.datetime.strftime(datetime.datetime.now(), '%d%m')+"&rev="+str(value1)
    urllib2.urlopen("http://7cfcc050.ngrok.com/insert.php?name=8122514058&date="+datetime.datetime.strftime(datetime.datetime.now(), '%d%m')+"&rev="+str(value1))

'''
    
