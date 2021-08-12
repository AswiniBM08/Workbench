--****************************************************************
--
--     Copyright Contributors to the GenevaERS Project.
-- SPDX-License-Identifier: Apache-2.0
--
--***********************************************************************
--*                                                                           
--*   Licensed under the Apache License, Version 2.0 (the "License");         
--*   you may not use this file except in compliance with the License.        
--*   You may obtain a copy of the License at                                 
--*                                                                           
--*     http://www.apache.org/licenses/LICENSE-2.0                            
--*                                                                           
--*   Unless required by applicable law or agreed to in writing, software     
--*   distributed under the License is distributed on an "AS IS" BASIS,       
--*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
--*   or implied.
--*   See the License for the specific language governing permissions and     
--*   limitations under the License.                                          
--***********************************************************************
CREATE OR REPLACE FUNCTION :schemaV.updateViewColumnSource (  
                                                IN XIN TEXT) RETURNS VOID
AS $$
DECLARE root_xpath TEXT = '/Root/Record/';
DECLARE DOC XML;
DECLARE OP RECORD;

BEGIN
 DOC = XMLPARSE (DOCUMENT XIN);

  FOR OP IN SELECT X.*
   FROM XMLTABLE ('/Root/Record' passing DOC 
   COLUMNS 
     ENVIRONID         INT PATH 'ENVIRONID',
     VIEWCOLUMNSOURCEID INT PATH 'VIEWCOLUMNSOURCEID',
     VIEWCOLUMNID      INT PATH 'VIEWCOLUMNID',
     VIEWSOURCEID      INT PATH 'VIEWSOURCEID',
     VIEWID            INT PATH 'VIEWID',
     SOURCETYPEID      INT PATH 'SOURCETYPEID',
     CONSTVAL          VARCHAR(255) PATH 'CONSTVAL',
     LOOKUPID          INT PATH 'LOOKUPID',
     LRFIELDID         INT PATH 'LRFIELDID',
     EFFDATEVALUE      VARCHAR(24) PATH 'EFFDATEVALUE',
     EFFDATETYPE       CHAR(5) PATH 'EFFDATETYPE',
     EFFDATELRFIELDID  INT PATH 'EFFDATELRFIELDID',
     SORTTITLELOOKUPID INT PATH 'SORTTITLELOOKUPID',     
     SORTTITLELRFIELDID INT PATH 'SORTTITLELRFIELDID',
     EXTRACTCALCLOGIC  TEXT PATH 'EXTRACTCALCLOGIC',
     LASTMODUSERID     VARCHAR(8) PATH 'LASTMODUSERID') AS X
  LOOP    
    UPDATE VIEWCOLUMNSOURCE SET
     SOURCETYPEID=OP.SOURCETYPEID,
     CONSTVAL=OP.CONSTVAL,
     LOOKUPID=OP.LOOKUPID,
     LRFIELDID=OP.LRFIELDID,
     EFFDATEVALUE=OP.EFFDATEVALUE,
     EFFDATETYPE=OP.EFFDATETYPE,
     EFFDATELRFIELDID=OP.EFFDATELRFIELDID,
     SORTTITLELOOKUPID=OP.SORTTITLELOOKUPID,
     SORTTITLELRFIELDID=OP.SORTTITLELRFIELDID,
     EXTRACTCALCLOGIC=OP.EXTRACTCALCLOGIC,
     LASTMODUSERID=OP.LASTMODUSERID,
     LASTMODTIMESTAMP=CURRENT_TIMESTAMP
    WHERE ENVIRONID=OP.ENVIRONID
    AND VIEWCOLUMNID=OP.VIEWCOLUMNID
    AND VIEWSOURCEID=OP.VIEWSOURCEID
    AND VIEWID=OP.VIEWID
    AND VIEWCOLUMNSOURCEID=OP.VIEWCOLUMNSOURCEID;
  END LOOP;

END
$$
LANGUAGE plpgsql;