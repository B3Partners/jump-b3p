



/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.wms;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.*;
import java.util.*;

/**
 * Represents all of the parameters of a getMap request from a WMS server.
 * @author Chris Hodgson chodgson@refractions.net
 */
public class MapRequest {
  private WMService service;
  private int imgWidth;
  private int imgHeight;
  private List layerList;
  private BoundingBox bbox;
  private boolean transparent;
  private String format;

  /**
   * Creates a new MapRequest.
   * @param service the WMService which this MapRequest will use
   */
  public MapRequest(WMService service) {
    this.service = service;
    imgWidth = 100;
    imgHeight = 100;
    layerList = new ArrayList();
    bbox = service.getCapabilities().getTopLayer().getBoundingBox();
    transparent = false;
    format = null;
  }

  /**
   * Gets the WMService that this object will make requests from.
   * @return the WMService that this object will make requests from
   */
  public WMService getService() {
    return service;
  }

  /**
   * Returns the format of this request.
   * This may be a string such as GIF, JPEG, or PNG for a WMS 1.0 server, or
   * a mime-type string in the case of a WMS 1.1 server. It may also be null if
   * the format has not yet been set.
   * @return the string representing the format of this request
   */
  public String getFormat() {
    return format;
  }

  /**
   * Gets the width of the requested image, in pixels.
   * The default image width is 100.
   * @return the width of the requested image
   */
  public int getImageWidth() {
    return imgWidth;
  }

  /**
   * Gets the height of the requested image, in pixels.
   * The default image height is 100.
   * @return the height of the requested image
   */
  public int getImageHeight() {
    return imgHeight;
  }

  /**
   * Returns the list of layers to be requested. Each item in the
   * list should be a String which is the name of a layer.
   * @return the list of layer names to be requested
   */
  public List getLayers() {
      //<<TODO:NAMING>> Might be clearer to name this method getLayerNames [Jon Aquino]
      return Collections.unmodifiableList(layerList);
  }

  /**
   * Gets the BoundingBox of the image being requested.
   * @return the BoundingBox of the image being requested
   */
  public BoundingBox getBoundingBox() {
    return bbox;
  }

  /**
   * Gets whether or not a transparent image is being requested.
   * @return true if a transparent image is being requested, false otherwise
   */
  public boolean getTransparent() {
    return transparent;
  }

  /**
   * Sets the format of this request. The format must be a string which is in
   * the list of supported formats as provided by getSupportedFormatList()
   * (not necessarily the same String object, but the same sequence of characters).
   * This will be an unformatted string for a WMS 1.0 server (GIF, JPEG, PNG) or
   * a mime-type string for a WMS 1.1 server (image/gif, image/jpeg, image/png).
   * If the format specified is not in the list, an IllegalArgumentException
   * will be thrown.
   *
   * @param format a format string which is in the list of supported formats
   * @see MapImageFormatChooser
   *
   */
  public void setFormat( String format ) throws IllegalArgumentException {
    String[] formats = service.getCapabilities().getMapFormats();
    for( int i = 0; i < formats.length; i++ ) {
      if( formats[i].equals( format ) ) {
        this.format = format;
        return;
      }
    }
    throw new IllegalArgumentException();
  }

  /**
   * Sets the width of the image being requested.
   * @param imageWidth the width of the image being requested
   */
  public void setImageWidth( int imageWidth ) {
    this.imgWidth = imageWidth;
  }

  /**
   * Sets the height of the image being requested.
   * @param imageHeight the height of the image being requested
   */
  public void setImageHeight( int imageHeight ) {
    this.imgHeight = imageHeight;
  }

  /**
   * Sets the width and height of the image being requested.
   * @param imageWidth the width of the image being requested
   * @param imageHeight the height of the image being requested
   */
  public void setImageSize( int imageWidth, int imageHeight ) {
    this.imgWidth = imageWidth;
    this.imgHeight = imageHeight;
  }

  /**
   * Sets the layers to be requested. Each item in the list should be a string
   * which corresponds to the name of a layer. The order of the list is
   * important as the layers are rendered in the same order they are listed.
   * @param layerList an ordered List of the names of layers to be displayed
   */
  public void setLayers( List layerList ) {
    //<<TODO:NAMING>> Might be clearer to name this method setLayerNames [Jon Aquino]
    this.layerList = layerList;
  }

  /**
   * Sets the BoundingBox of the image being requested.
   * @param bbox the BoundingBox of the image being requested
   */
  public void setBoundingBox( BoundingBox bbox ) {
    this.bbox = bbox;
  }

  /**
   * Sets whether or not to request an image with a transparent background.
   * Requesting a transparent background doesn't guarantee that the resulting
   * image will actually have a transparent background. Not all servers
   * support transparency, and not all formats support transparency.
   * @param transparent true to request a transparent background, false otherwise.
   */
  public void setTransparent( boolean transparent ) {
    this.transparent = transparent;
  }

  /**
   * Returns a String containing the string representations of each item in the
   * list (as provided by toString()), separated by commas.
   * @param list the list to be returned as a coma-separated String
   * @return a comma-separted String of the items in the list
   */
  private String listToString( List list ) {
    Iterator it = list.iterator();
    StringBuffer buf = new StringBuffer();
    while( it.hasNext() ) {
      String layer = (String)it.next();
      //Unfortunately we can't use the new two-parameter constructor because
      //that was introduced in JDK 1.4, and JUMP is supposed to support 1.3. [Jon Aquino]
      layer = URLEncoder.encode( layer );
//      try {
//        layer = URLEncoder.encode( layer, "UTF-8" );
//      } catch( UnsupportedEncodingException uee ) {
//        // ok then, we just won't bother to encode at all...
//      }
      buf.append( layer );
      if( it.hasNext() ) {
        buf.append( "," );
      }
    }
    return buf.toString();
  }

  public URL getURL() throws MalformedURLException {
    StringBuffer urlBuf = new StringBuffer();
    urlBuf.append( service.getServerUrl() + "REQUEST=map&WMTVER=1.0&WIDTH=" + imgWidth + "&HEIGHT=" + imgHeight );
    urlBuf.append( "&LAYERS=" + listToString( layerList ) );
    if( transparent ) {
      urlBuf.append( "&TRANSPARENT=TRUE" );
    }
    if( format != null ) {
      urlBuf.append( "&FORMAT=" + format );
    }
    if( bbox != null ) {
      urlBuf.append( "&BBOX=" + bbox.getMinX() + "," + bbox.getMinY()
                    + "," + bbox.getMaxX() + "," + bbox.getMaxY() );
      if( bbox.getSRS() != null && !bbox.getSRS().equals( "LatLon" ) ) {
        urlBuf.append( "&SRS=" + bbox.getSRS() );
      }
    }
    return new URL( urlBuf.toString() );
  }

  /**
   * Connect to the service and get an Image of the map.
   * @return the retrieved map Image
   */
  public Image getImage() throws MalformedURLException {
    return Toolkit.getDefaultToolkit().createImage( getURL() );

    /* for using local file method of download
    InputStream inStream = requestUrl.openStream();
    File tempImage = File.createTempFile( "wms", ".img");
    System.out.println( tempImage.getAbsolutePath() );
    FileOutputStream outStream = new FileOutputStream( tempImage );
    byte[] bytes = new byte[1024];
    int count;
    while( (count = inStream.read( bytes )) > 0 ) {
      outStream.write( bytes, 0, count );
    }
    outStream.close();
    Image img = Toolkit.getDefaultToolkit().createImage( tempImage.getAbsolutePath() );
    // tempImage.delete(); // this breaks the asynchronous loadin used in the above line
    */

  }

}
