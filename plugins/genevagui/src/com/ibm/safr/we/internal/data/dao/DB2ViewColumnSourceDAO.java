package com.ibm.safr.we.internal.data.dao;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.UserSessionParameters;
import com.ibm.safr.we.data.dao.ViewColumnSourceDAO;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.internal.data.SQLGenerator;
import com.ibm.safr.we.model.SAFRApplication;

public class DB2ViewColumnSourceDAO implements ViewColumnSourceDAO {

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.internal.data.dao.DB2ViewColumnSourceDAO");

    @SuppressWarnings("unused")
    private static final String TABLE_NAME = "VIEWCOLUMNSOURCE";	
	private static final String COL_ENVID = "ENVIRONID";
	private static final String COL_ID = "VIEWCOLUMNSOURCEID";
	private static final String COL_VIEWID = "VIEWID";
    private static final String COL_CREATETIME = "CREATEDTIMESTAMP";
    private static final String COL_CREATEBY = "CREATEDUSERID";
    private static final String COL_MODIFYTIME = "LASTMODTIMESTAMP";
    private static final String COL_MODIFYBY = "LASTMODUSERID";

	private Connection con;
	private ConnectionParameters params;
	private UserSessionParameters safrLogin;
	private SQLGenerator generator = new SQLGenerator();

	/**
	 * Constructor for this class.
	 * 
	 * @param con
	 *            : The connection set for database access.
	 * @param params
	 *            : The connection parameters which define the URL, userId and
	 *            other details of the connection.
	 * @param safrLogin
	 *            : The parameters related to the user who has logged into the
	 *            workbench.
	 */
	public DB2ViewColumnSourceDAO(Connection con, ConnectionParameters params,
			UserSessionParameters safrLogin) {
		this.con = con;
		this.params = params;
		this.safrLogin = safrLogin;
	}

	private ViewColumnSourceTransfer generateTransfer(ResultSet rs)
			throws SQLException {
		ViewColumnSourceTransfer vcsTransfer = new ViewColumnSourceTransfer();
		vcsTransfer.setEnvironmentId(rs.getInt(COL_ENVID));
		vcsTransfer.setId(rs.getInt(COL_ID));
		vcsTransfer.setViewColumnId(rs.getInt("VIEWCOLUMNID"));
		vcsTransfer.setViewSourceId(rs.getInt("VIEWSOURCEID"));
        vcsTransfer.setViewId(rs.getInt(COL_VIEWID));
		vcsTransfer.setSourceTypeId(rs.getInt("SOURCETYPEID"));
        vcsTransfer.setSourceValue(rs.getString("CONSTVAL"));
        vcsTransfer.setLookupPathId(rs.getInt("LOOKUPID"));
		vcsTransfer.setSourceLRFieldId(rs.getInt("LRFIELDID"));
        vcsTransfer.setEffectiveDateValue(DataUtilities.trimString(rs.getString("EFFDATEVALUE")));
        vcsTransfer.setEffectiveDateTypeCode(DataUtilities.trimString(rs.getString("EFFDATETYPE")));
        vcsTransfer.setEffectiveDateLRFieldId(rs.getInt("EFFDATELRFIELDID"));
        vcsTransfer.setSortKeyTitleLookupPathId(rs.getInt("SORTTITLELOOKUPID"));
		vcsTransfer.setSortKeyTitleLRFieldId(rs.getInt("SORTTITLELRFIELDID"));
        Clob clob = rs.getClob("EXTRACTCALCLOGIC");
        if (clob != null) {
            vcsTransfer.setExtractColumnLogic(clob.getSubString(1, (int) clob.length()));
        }
		vcsTransfer.setCreateTime(rs.getDate(COL_CREATETIME));
		vcsTransfer.setCreateBy(DataUtilities.trimString(rs.getString(COL_CREATEBY)));
		vcsTransfer.setModifyTime(rs.getDate(COL_MODIFYTIME));
		vcsTransfer.setModifyBy(DataUtilities.trimString(rs.getString(COL_MODIFYBY)));
		
		return vcsTransfer;
	}

	public List<ViewColumnSourceTransfer> getViewColumnSources(Integer viewId,
			Integer environmentId) throws DAOException {
		List<ViewColumnSourceTransfer> viewColumnSources = new ArrayList<ViewColumnSourceTransfer>();
		try {

			String selectString = "Select A.ENVIRONID, A.VIEWCOLUMNSOURCEID, A.VIEWCOLUMNID, A.VIEWSOURCEID, "
					+ "A.VIEWID, A.SOURCETYPEID, A.CONSTVAL, A.LOOKUPID, A.LRFIELDID, "
					+ "A.EFFDATEVALUE, A.EFFDATETYPE, A.EFFDATELRFIELDID, "
					+ "A.SORTTITLELOOKUPID, A.SORTTITLELRFIELDID, A.EXTRACTCALCLOGIC, " 
                    + "A.CREATEDTIMESTAMP, A.CREATEDUSERID, A.LASTMODTIMESTAMP, A.LASTMODUSERID "                  
					+ " From "
					+ params.getSchema() + ".VIEWCOLUMNSOURCE A, " 
					+ params.getSchema() + ".VIEWSOURCE B, "
                    + params.getSchema() + ".VIEWCOLUMN C "
					+ " WHERE A.ENVIRONID =? AND A.ENVIRONID = B.ENVIRONID";
			if(viewId > 0) {
				selectString += " AND A.VIEWID = ? ";
			}
			selectString +=" AND A.VIEWSOURCEID = B.VIEWSOURCEID"
                    + " AND A.ENVIRONID = C.ENVIRONID"
                    + " AND A.VIEWCOLUMNID = C.VIEWCOLUMNID"
					+ " ORDER BY B.SRCSEQNBR, C.COLUMNNUMBER";
			PreparedStatement pst = null;
			ResultSet rs = null;
			while (true) {
				try {
					pst = con.prepareStatement(selectString);
					pst.setInt(1, environmentId);
					if (viewId > 0) {
						pst.setInt(2, viewId);
					}
					rs = pst.executeQuery();
					break;
				} catch (SQLException se) {
					if (con.isClosed()) {
						// lost database connection, so reconnect and retry
						con = DAOFactoryHolder.getDAOFactory().reconnect();
					} else {
						throw se;
					}
				}
			}
			while (rs.next()) {
				ViewColumnSourceTransfer vcSrcTransfer = new ViewColumnSourceTransfer();
				vcSrcTransfer = generateTransfer(rs);
				viewColumnSources.add(vcSrcTransfer);
			}
			pst.close();
			rs.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while retrieving View Column Sources for the View with id ["+ viewId + "]", e);
		}
		return viewColumnSources;
	}

    public int getViewColumnSourceLrId(Integer viewColSrcId, Integer environmentId) throws DAOException {
        int result = 0;
        try {
    
            String selectString = "SELECT A.LOGRECID FROM " +
                params.getSchema() + ".LRLFASSOC A," +
                params.getSchema() + ".VIEWSOURCE B," +
                params.getSchema() + ".VIEWCOLUMNSOURCE C " +
                "WHERE A.ENVIRONID=B.ENVIRONID " + 
                "AND A.LRLFASSOCID=B.INLRLFASSOCID " +
                "AND B.ENVIRONID=C.ENVIRONID " +
                "AND B.VIEWSOURCEID=C.VIEWSOURCEID " +
                "AND C.VIEWCOLUMNSOURCEID=? " +
                "AND C.ENVIRONID=?";
            PreparedStatement pst = null;
            ResultSet rs = null;
            while (true) {
                try {
                    pst = con.prepareStatement(selectString);
                    pst.setInt(1, viewColSrcId);
                    pst.setInt(2, environmentId);
                    rs = pst.executeQuery();
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                }
            }
            if (rs.next()) {
                result = rs.getInt(1);
            }
            pst.close();
            rs.close();
        } catch (SQLException e) {
            throw DataUtilities.createDAOException(
                "Database error occurred while retrieving View Column Source with id ["+ viewColSrcId + "]", e);
        }
        return result;
    }
	
	public List<ViewColumnSourceTransfer> persistViewColumnSources(
			List<ViewColumnSourceTransfer> viewColSrcTransferList)
			throws DAOException {

		if (viewColSrcTransferList == null || viewColSrcTransferList.isEmpty()) {
			return viewColSrcTransferList;
		}
        List<ViewColumnSourceTransfer> ret = new ArrayList<ViewColumnSourceTransfer>();
				
		List<ViewColumnSourceTransfer> viewColSrcCreate = new ArrayList<ViewColumnSourceTransfer>();
		List<ViewColumnSourceTransfer> viewColSrcUpdate = new ArrayList<ViewColumnSourceTransfer>();

		int countCreate = 0;
		boolean fCreProc = false;
        int countUpdate = 0;
        boolean fUpProc = false;
		for (ViewColumnSourceTransfer viewColTrans : viewColSrcTransferList) {
		    if (countCreate % 500 == 0 && fCreProc) {
	            viewColSrcCreate = createViewColumnSources(viewColSrcCreate);
	            ret.addAll(viewColSrcCreate);		        
		        viewColSrcCreate.clear();
		        fCreProc = false;
		    }
            if (countUpdate % 500 == 0 && fUpProc) {
                viewColSrcUpdate = updateViewColumnSources(viewColSrcUpdate);
                ret.addAll(viewColSrcUpdate);               
                viewColSrcUpdate.clear();
                fUpProc = false;                
            }
			if (!viewColTrans.isPersistent()) {
                fCreProc = true;
                countCreate++;
				viewColSrcCreate.add(viewColTrans);
			} else {
                fUpProc = true;
                countUpdate++;
				viewColSrcUpdate.add(viewColTrans);
			}
		}
		if (viewColSrcCreate.size() > 0) {
			viewColSrcCreate = createViewColumnSources(viewColSrcCreate);
            ret.addAll(viewColSrcCreate);               
		}
		if (viewColSrcUpdate.size() > 0) {
			viewColSrcUpdate = updateViewColumnSources(viewColSrcUpdate);
            ret.addAll(viewColSrcUpdate);               
		}
		return ret;
	}

    private void getXml(ViewColumnSourceTransfer srcFld, StringBuffer buf) throws SQLException, DAOException {
        buf.append("  <VIEWCOLUMNID>"+ srcFld.getViewColumnId() + "</VIEWCOLUMNID>\n");
        buf.append("  <VIEWSOURCEID>"+ srcFld.getViewSourceId() + "</VIEWSOURCEID>\n");
        buf.append("  <VIEWID>"+ srcFld.getViewId() + "</VIEWID>\n");
        buf.append("  <SOURCETYPEID>"+ srcFld.getSourceTypeId() + "</SOURCETYPEID>\n");
        if (srcFld.getSourceValue() == null) {
            buf.append("  <CONSTVAL></CONSTVAL>\n");
        }
        else {
            String str = generator.handleSpecialChars(srcFld.getSourceValue());
            buf.append("  <CONSTVAL>"+ str + "</CONSTVAL>\n");                
        }
        if (srcFld.getLookupPathId() == null) {
            buf.append("  <LOOKUPID>0</LOOKUPID>\n");                                                
        }
        else {
            buf.append("  <LOOKUPID>"+ srcFld.getLookupPathId() + "</LOOKUPID>\n");                                
        }                        
        if (srcFld.getSourceLRFieldId() != null) {
            buf.append("  <LRFIELDID>"+ srcFld.getSourceLRFieldId() + "</LRFIELDID>\n");                
        }
        if (srcFld.getEffectiveDateValue() != null) {
            buf.append("  <EFFDATEVALUE>"+ srcFld.getEffectiveDateValue() + "</EFFDATEVALUE>\n");                
        }
        if (srcFld.getEffectiveDateTypeCode() != null) {
            buf.append("  <EFFDATETYPE>"+ srcFld.getEffectiveDateTypeCode() + "</EFFDATETYPE>\n");                
        }
        if (srcFld.getEffectiveDateLRFieldId() == null) {
            buf.append("  <EFFDATELRFIELDID>0</EFFDATELRFIELDID>\n");                
        }
        else {
            buf.append("  <EFFDATELRFIELDID>"+ srcFld.getEffectiveDateLRFieldId() + "</EFFDATELRFIELDID>\n");                                
        }
        if (srcFld.getSortKeyTitleLookupPathId() == null) {
            buf.append("  <SORTTITLELOOKUPID>0</SORTTITLELOOKUPID>\n");                                                
        }
        else {
            buf.append("  <SORTTITLELOOKUPID>"+ srcFld.getSortKeyTitleLookupPathId() + "</SORTTITLELOOKUPID>\n");                                
        }                                
        if (srcFld.getSortKeyTitleLRFieldId() == null) {
            buf.append("  <SORTTITLELRFIELDID>0</SORTTITLELRFIELDID>\n");                                                
        }
        else {
            buf.append("  <SORTTITLELRFIELDID>"+ srcFld.getSortKeyTitleLRFieldId() + "</SORTTITLELRFIELDID>\n");                                
        }    
        if (srcFld.getExtractColumnLogic() != null) {
            buf.append("  <EXTRACTCALCLOGIC><![CDATA["+ srcFld.getExtractColumnLogic() + "]]></EXTRACTCALCLOGIC>\n");                                            
        }        
    }
	
    private String getCreateXml(List<ViewColumnSourceTransfer> srcFlds) throws SQLException, DAOException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("<Root>\n");
        for (ViewColumnSourceTransfer srcFld : srcFlds) {
            buf.append(" <Record>\n");
            buf.append("  <ENVIRONID>"+ srcFld.getEnvironmentId() + "</ENVIRONID>\n");
            if (srcFld.isForImportOrMigration()) {
                buf.append("  <VIEWCOLUMNSOURCEID>"+ srcFld.getId() + "</VIEWCOLUMNSOURCEID>\n");
            }
            getXml(srcFld, buf);
            
            if (srcFld.isForImportOrMigration()) {
                buf.append("  <CREATEDTIMESTAMP>"+ generator.genTimeParm(srcFld.getCreateTime()) + "</CREATEDTIMESTAMP>\n");
                buf.append("  <CREATEDUSERID>"+ srcFld.getCreateBy() + "</CREATEDUSERID>\n");
                buf.append("  <LASTMODTIMESTAMP>"+ generator.genTimeParm(srcFld.getModifyTime()) + "</LASTMODTIMESTAMP>\n");
                buf.append("  <LASTMODUSERID>"+ srcFld.getModifyBy() + "</LASTMODUSERID>\n");
            }
            else {
                buf.append("  <CREATEDUSERID>"+ safrLogin.getUserId() + "</CREATEDUSERID>\n");
                buf.append("  <LASTMODUSERID>"+ safrLogin.getUserId() + "</LASTMODUSERID>\n");
            }            
            buf.append(" </Record>\n");            
        }
        buf.append("</Root>");
        return buf.toString();
    }   

    private String getUpdateXml(List<ViewColumnSourceTransfer> srcFlds) throws SQLException, DAOException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("<Root>\n");
        for (ViewColumnSourceTransfer srcFld : srcFlds) {
            buf.append(" <Record>\n");
            buf.append("  <ENVIRONID>"+ srcFld.getEnvironmentId() + "</ENVIRONID>\n");
            buf.append("  <VIEWCOLUMNSOURCEID>"+ srcFld.getId() + "</VIEWCOLUMNSOURCEID>\n");
            getXml(srcFld, buf);
            
            if (srcFld.isForImportOrMigration()) {
                buf.append("  <CREATEDTIMESTAMP>"+ generator.genTimeParm(srcFld.getCreateTime()) + "</CREATEDTIMESTAMP>\n");
                buf.append("  <CREATEDUSERID>"+ srcFld.getCreateBy() + "</CREATEDUSERID>\n");
                buf.append("  <LASTMODTIMESTAMP>"+ generator.genTimeParm(srcFld.getModifyTime()) + "</LASTMODTIMESTAMP>\n");
                buf.append("  <LASTMODUSERID>"+ srcFld.getModifyBy() + "</LASTMODUSERID>\n");
            }
            else {
                buf.append("  <LASTMODUSERID>"+ safrLogin.getUserId() + "</LASTMODUSERID>\n");
            }            
            buf.append(" </Record>\n");            
        }
        buf.append("</Root>");
        return buf.toString();
    }   
    
	private List<ViewColumnSourceTransfer> createViewColumnSources(
			List<ViewColumnSourceTransfer> viewColSrcCreateList)
			throws DAOException {
		try {

		    SAFRApplication.getTimingMap().startTiming("DB2ViewColumnSourceDAO.createViewColumnSources");
		    
			String statement = generator.getReturnStoredProcedure(params.getSchema(),"INSVIEWCOLSRC", 2);

			CallableStatement proc = null;

            while (true) {
                try {
                    proc = con.prepareCall(statement);
                    proc.registerOutParameter(1, Types.INTEGER);
                    String xml = getCreateXml(viewColSrcCreateList);
                    proc.setString(2, xml);
                    if (viewColSrcCreateList.isEmpty() || !viewColSrcCreateList.get(0).isForImportOrMigration()) {
                        proc.setInt(3, 0);                
                    }
                    else {
                        proc.setInt(3, 1);                                
                    }                   
                    proc.execute();
                    proc.close();
                    
                    // break up list into different sources
                    Map<Integer, List<ViewColumnSourceTransfer>> colSrcMap = new HashMap<Integer, List<ViewColumnSourceTransfer>>();
                    for (ViewColumnSourceTransfer colSrc : viewColSrcCreateList) {
                        if (!colSrcMap.containsKey(colSrc.getViewSourceId())) {
                            colSrcMap.put(colSrc.getViewSourceId(), new ArrayList<ViewColumnSourceTransfer>());                            
                        }
                        colSrcMap.get(colSrc.getViewSourceId()).add(colSrc);
                    }
                    
                    // iterate sources
                    for (Map.Entry<Integer, List<ViewColumnSourceTransfer>> entry : colSrcMap.entrySet()) {
                        Map<Integer, Integer> colIDMap = getColIDMap(entry.getKey(), entry.getValue());
                        
                        for (ViewColumnSourceTransfer colSrc : entry.getValue()) {
                            colSrc.setPersistent(true);
                            if (!colSrc.isForImportOrMigration()) {
                                colSrc.setId(colIDMap.get(colSrc.getViewColumnId()));
                            }
                        }                                            
                    }                    
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                }
            }
		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while creating new View Column Sources.",e);
		}
		SAFRApplication.getTimingMap().stopTiming("DB2ViewColumnSourceDAO.createViewColumnSources");
		
		return viewColSrcCreateList;
	}

    protected Map<Integer, Integer> getColIDMap(Integer viewSourceId, List<ViewColumnSourceTransfer> viewColCreateList) throws SQLException {
        
        Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();
        String placeholders = generator.getPlaceholders(viewColCreateList.size());
        int viewID = viewColCreateList.get(0).getViewId();
        int envID = viewColCreateList.get(0).getEnvironmentId();
        
        String chkSt = "select viewcolumnid,viewcolumnsourceid " +
            "from " + params.getSchema() + ".viewcolumnsource " +
            "where environid= ? " +
            "and viewid= ? " +
            "and viewsourceid= ? " +
            "and viewcolumnid in ( " + placeholders + ")";                    
        PreparedStatement pst = null;
        ResultSet rs = null;
        pst = con.prepareCall(chkSt);
        int ndx = 1;
        pst.setInt(ndx++, envID);
        pst.setInt(ndx++, viewID);
        pst.setInt(ndx++, viewSourceId);
        for(int i=0; i<viewColCreateList.size(); i++) {
        	pst.setInt(ndx++, viewColCreateList.get(i).getViewColumnId());
        }
        rs = pst.executeQuery();                    
        // form map of column number to id
        while (rs.next()) {
            Integer colId = rs.getInt(1);
            Integer id = rs.getInt(2);
            idMap.put(colId, id);
        }
        rs.close();
        pst.close();
        
        return idMap;
    }
	
	private List<ViewColumnSourceTransfer> updateViewColumnSources(
			List<ViewColumnSourceTransfer> viewColSrcUpdateList)
			throws DAOException {
		try {
		    SAFRApplication.getTimingMap().startTiming("DB2ViewColumnSourceDAO.updateViewColumnSources");
		    
			String statement = generator.getStoredProcedure(params.getSchema(),
					"UPDVIEWCOLSRC", 2);
			CallableStatement proc = null;

            while (true) {
                try {
                    proc = con.prepareCall(statement);
                    String xml = getUpdateXml(viewColSrcUpdateList);
                    proc.setString("DOC", xml);
                    if (viewColSrcUpdateList.isEmpty() || !viewColSrcUpdateList.get(0).isForImportOrMigration()) {
                        proc.setInt("IMPORT", 0);                
                    }
                    else {
                        proc.setInt("IMPORT", 1);                                
                    }                   
                    proc.execute();
                    for (ViewColumnSourceTransfer colSrc : viewColSrcUpdateList) {
                        colSrc.setPersistent(true);
                    }
                    break;
                } catch (SQLException se) {
                    if (con.isClosed()) {
                        // lost database connection, so reconnect and retry
                        con = DAOFactoryHolder.getDAOFactory().reconnect();
                    } else {
                        throw se;
                    }
                }
            }
			proc.close();
		} catch (SQLException e) {
			throw DataUtilities.createDAOException(
			    "Database error occurred while updating View Column Sources.",e);
		}
		
		SAFRApplication.getTimingMap().stopTiming("DB2ViewColumnSourceDAO.updateViewColumnSources");
		
		return viewColSrcUpdateList;
	}

	public void removeViewColumnSources(List<Integer> vwColumnSrcIds,
			Integer environmentId) throws DAOException {
		if (vwColumnSrcIds == null || vwColumnSrcIds.size() == 0) {
			return;
		}
		try {
			String placeholders = generator.getPlaceholders(vwColumnSrcIds.size()); 

			String deleteColSourcesQuery = "Delete From " + params.getSchema()
					+ ".VIEWCOLUMNSOURCE Where VIEWCOLUMNSOURCEID IN (" 
					+ placeholders + ") AND ENVIRONID = ? ";
			PreparedStatement pst = null;

			while (true) {
				try {
					pst = con.prepareStatement(deleteColSourcesQuery);
					int ndx = 1;
					for(int i=0; i<vwColumnSrcIds.size(); i++) {
						pst.setInt(ndx++, vwColumnSrcIds.get(i));
					}
					pst.setInt(ndx, environmentId);
					pst.executeUpdate();
					break;
				} catch (SQLException se) {
					if (con.isClosed()) {
						// lost database connection, so reconnect and retry
						con = DAOFactoryHolder.getDAOFactory().reconnect();
					} else {
						throw se;
					}
				}
			}
			pst.close();

		} catch (SQLException e) {
			throw DataUtilities.createDAOException("Database error occurred while deleting View Column Sources.",e);
		}
	}
}
