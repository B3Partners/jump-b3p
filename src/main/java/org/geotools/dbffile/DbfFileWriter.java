package org.geotools.dbffile;

import java.io.*;
import java.util.*;

import org.geotools.misc.FormatedString;

import com.vividsolutions.jump.io.EndianDataOutputStream;



/** a class for writing dbf files

 * @author Ian Turton

 */



public class DbfFileWriter implements DbfConsts{



	private final static boolean DEBUG=false;

	private final static String DBC="DbFW>";

  int NoFields =1;

  int NoRecs = 0;

  int recLength = 0;

  DbfFieldDef fields[];

  EndianDataOutputStream ls;

	private boolean header = false;

  public DbfFileWriter(String file) throws IOException{

	if(DEBUG)System.out.println("---->uk.ac.leeds.ccg.dbffile.DbfFileWriter constructed. Will identify itself as "+DBC);

	ls = new EndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

  }

  public void writeHeader(DbfFieldDef f[], int nrecs) throws IOException{

    NoFields = f.length;

    NoRecs = nrecs;

    fields = new DbfFieldDef[NoFields];

    for(int i=0;i<NoFields;i++){

      fields[i]=f[i];

    }

    ls.writeByteLE(3); // ID - dbase III with out memo

    // sort out the date

    Calendar calendar = new GregorianCalendar();

    Date trialTime = new Date();

    calendar.setTime(trialTime);

    ls.writeByteLE(calendar.get(Calendar.YEAR) - DBF_CENTURY);

    ls.writeByteLE(calendar.get(Calendar.MONTH) + 1 ); // month is 0-indexed


    ls.writeByteLE(calendar.get(Calendar.DAY_OF_MONTH));

    int dataOffset = 32 * NoFields+32 + 1;

    for(int i=0;i<NoFields;i++){

      recLength+=fields[i].fieldlen;

    }

    recLength++; // delete flag

    if(DEBUG)System.out.println(DBC+"rec length "+recLength);

    ls.writeIntLE(NoRecs);

    ls.writeShortLE(dataOffset); // length of header

    ls.writeShortLE(recLength);

    for(int i=0;i<20;i++) ls.writeByteLE(0); // 20 bytes of junk!

    // field descriptions

    for(int i=0;i<NoFields;i++){





      ls.writeBytesLE(fields[i].fieldname.toString());

      ls.writeByteLE(fields[i].fieldtype);

      for(int j=0;j<4;j++) ls.writeByteLE(0); // junk

      ls.writeByteLE(fields[i].fieldlen);

      ls.writeByteLE(fields[i].fieldnumdec);

      for(int j=0;j<14;j++) ls.writeByteLE(0); // more junk

    }

    ls.writeByteLE(0xd);

  	header=true;

  }

  public void writeRecords(Vector [] recs) throws DbfFileException,IOException{

		if(!header){

			throw(new DbfFileException("Must write header before records"));

		}

    int i=0;

    try{

			if(DEBUG)System.out.println(DBC+":writeRecords writing "+recs.length+" records");

      for(i=0;i<recs.length;i++){

			if(recs[i].size()!=NoFields) throw new DbfFileException("wrong number of records in "+ i+"th record "+

      recs[i].size()+" expected "+NoFields);

        writeRecord(recs[i]);

      }

    }catch(DbfFileException e){throw new DbfFileException(DBC+"at rec "+i+"\n"+e);}

  }

  public void writeRecord(Vector rec)throws DbfFileException,IOException{

		if(!header){

			throw(new DbfFileException(DBC+"Must write header before records"));

		}

    if(rec.size()!=NoFields) throw new DbfFileException(DBC+"wrong number of fields "+

      rec.size()+" expected "+NoFields);

    String s;

    ls.writeByteLE(' ');

		int len;

		StringBuffer tmps;

    for(int i=0;i<NoFields;i++){

      len = fields[i].fieldlen;

      Object o = rec.elementAt(i);



      switch(fields[i].fieldtype){

        case 'C':

        case 'c':

        case 'D': //Added by [Jon Aquino]

        case 'L':

        case 'M':

        case 'G':

          //chars

                    String ss = (String) o;

                    while  (ss.length() < fields[i].fieldlen)

                    {

                            //need to fill it with ' ' chars

                                //this should converge quickly

                            ss = ss + "                                                                                                                  ";

                    }



					tmps = new StringBuffer(ss);

					tmps.setLength(fields[i].fieldlen);

          ls.writeBytesLE(tmps.toString());

          break;

        case 'N':

        case 'n':

          // int?

          if(fields[i].fieldnumdec==0){

						ls.writeBytesLE(FormatedString.format(((Integer)o).intValue(),fields[i].fieldlen));

						break;

					}

        case 'F':

        case 'f':

          //double

          s = ((Double)o).toString();

					String x = FormatedString.format(s,fields[i].fieldnumdec,fields[i].fieldlen);

          ls.writeBytesLE(x);

          break;

      }// switch

    }// fields

  }

  public void close() throws IOException{

    ls.writeByteLE(0x1a); // eof mark

    ls.close();

  }

	int dp = 2; // default number of decimals to write



}

