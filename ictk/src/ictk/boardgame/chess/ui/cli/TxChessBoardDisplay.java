/*
 *  ICTK - Internet Chess ToolKit
 *  More information is available at http://ictk.sourceforge.net
 *  Copyright (C) 2002 J. Varsoke <jvarsoke@ghostmanonfirst.com>
 *  All rights reserved.
 *
 *  $Id$
 *
 *  This file is part of ICTK.
 *
 *  ICTK is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  ICTK is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ICTK; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package ictk.boardgame.chess.ui.cli;

import ictk.boardgame.Board;
import ictk.boardgame.BoardListener;
import ictk.boardgame.BoardEvent;
import ictk.boardgame.chess.ChessBoard;
import ictk.boardgame.chess.ChessPiece;
import ictk.boardgame.chess.Square;
import ictk.boardgame.chess.ui.ChessBoardDisplay;
import ictk.boardgame.chess.io.ChessMoveNotation;
import ictk.boardgame.chess.io.SAN;

import java.io.*;


/* TxChessBoardDisplay ******************************************************/
/** This is a TxChess style command-line visual representation of a board.
 *  TxChess is an old C program used for email chess.
 */
public class TxChessBoardDisplay implements CLIChessBoardDisplay,
                                            BoardListener {

      /** where to send the board display */
   protected PrintWriter out = new PrintWriter(System.out, true);

      /** the chessboard model this object is the view of */
   protected ChessBoard board;

      /** notation is used to help localize board piece characters */
   protected ChessMoveNotation notation = new SAN();

      /** is the board currently oriented so white is on the bottom */
   protected boolean whiteOnBottom = true,

      /** should the board re-orient itself so the player to move is always
       ** on the bottom. */
                     sideToMoveOnBottom = false;

      /** mask for which coordinates should be visible.
       ** Defaults to LEFT and BOTTOM */
   protected int coordMask = LEFT_COORDINATES | BOTTOM_COORDINATES;

      /** should the output use as few spaces as possible? */
   protected boolean compact,
      /** if true, then a black on white screen will be assumed */
                     inverse,
      /** present coordinates in lowercase */
		     lowercaseCoords;

      /** this is used to keep track of whether events we're receiving
       ** are part of a traversal or not. */
      protected boolean waitingForTraversalEnd = false;


   //constructors/////////////////////////////////////////////////////////////
   public TxChessBoardDisplay (ChessBoard board, OutputStream out) {
      this(board, new PrintWriter(out));
   }

   public TxChessBoardDisplay (ChessBoard board, Writer out) {
      this(board, new PrintWriter(out, true));
   }

   public TxChessBoardDisplay (ChessBoard board, PrintWriter out) {
      this.board = board;
      this.out = out;
   }

   public TxChessBoardDisplay (ChessBoard board) {
      this.board = board;
   }

   //BoardListenerd Interface/////////////////////////////////////////////////
   /** the board display is updated (printed to stream) for every event
    *  except durning traversal, in which only the final position causes
    *  a new board to be displayed.
    */
   public void boardUpdate (Board b, int event) { 
      if (event == BoardEvent.TRAVERSAL_BEGIN)
         waitingForTraversalEnd = true;

      if (!waitingForTraversalEnd)
         update(); 

      if (event == BoardEvent.TRAVERSAL_END)
         waitingForTraversalEnd = false;
   }
   //instance/////////////////////////////////////////////////////////////////

   /* setCompact ************************************************************/
   /** setting this attribute to true will eliminate needless space 
    *  characters in the output.
    *  <br>
    *  Default: false.
    */
   public void setCompact (boolean t) { compact = t; }

   /* isCompact *************************************************************/
   /** test if this display is in Compact mode 
    */
   public boolean isCompact () { return compact; }

   //BoardDisplay Interface///////////////////////////////////////////////////
   public void setBoard (Board board) { this.board = (ChessBoard) board; }

   public Board getBoard () { return board; }

   public void update () { print(); }

   //ChessBoardDisplay Interface//////////////////////////////////////////////
   public void setWhiteOnBottom (boolean t) { whiteOnBottom = t; }

   public boolean isWhiteOnBottom () { return whiteOnBottom; }

   public void setSideToMoveOnBottom (boolean t) { sideToMoveOnBottom = t; }

   public boolean getSideToMoveOnBottom () { return sideToMoveOnBottom; }

   public void setVisibleCoordinates (int mask) { coordMask = mask; }

   public int getVisibleCoordinates () { return coordMask; }

   public void setLowerCaseCoordinates (boolean t) { lowercaseCoords = t; }

   public boolean isLowerCaseCoordinates () { return lowercaseCoords; }

   //CLIBoardDisplay Interface/////////////////////////////////////////////////

   /* getInverse *************************************************************/
   public void setInverse (boolean t) { inverse = t; }

   /* isInverse **************************************************************/
   public boolean isInverse () { return inverse; }

   /* setWriter **************************************************************/
   public void setWriter (PrintWriter out) {
      this.out = out;
   }

   /* getWriter **************************************************************/
   public Writer getWriter () {
      return out;
   }

   /* print ******************************************************************/
   public void print () {
      print(board);
   }

   /* print ******************************************************************/
   public void print (Board board) {
      ChessBoard   b = (ChessBoard) board;
      Square       sq = null;
      StringBuffer last_line = null; 
      char         c = ' ';
      int          r, 
                   f,
		   x; //dummy var to satisfy the picky java compiler
      boolean top = (coordMask & TOP_COORDINATES) == TOP_COORDINATES,
              left = (coordMask & LEFT_COORDINATES) == LEFT_COORDINATES,
	      right = (coordMask & RIGHT_COORDINATES) == RIGHT_COORDINATES,
	      bottom = (coordMask & BOTTOM_COORDINATES) == BOTTOM_COORDINATES,
	      flipped = (!whiteOnBottom && !sideToMoveOnBottom)
	                || (b.isBlackMove() && sideToMoveOnBottom);
      char blackSquare = ((inverse) ? '#' : ' '),
           whiteSquare = ((inverse) ? ' ' : '#');

         //make the top coordinate line
	 if (top) {
	    if (left) {
	       out.print("  ");
	       if (!compact)
	          out.print("  ");
	    }

            r = 1;
            for (f=((flipped) ? b.MAX_FILE : 1); 
	         ((flipped) ? f >= 1 : f <= b.MAX_FILE); 
		 x = ((flipped) ? f-- : f++)) {

	       sq = b.getSquare(f, r);

               if (lowercaseCoords)
	          out.print(Character.toLowerCase(
		     notation.fileToChar(sq.getFile())));
	       else
		  out.print(Character.toUpperCase(
		     notation.fileToChar(sq.getFile())));

	       if (!compact && f != ((flipped) ? 1 : b.MAX_FILE))
	          out.print(" ");
	    }
	    out.println();
	    out.println();
	 }
        
	 //setup bottom coordinate line
         if (bottom) {
	    last_line = new StringBuffer((compact) ? 10 : 20);
	    last_line.append("\n");  
	    if (left) {
	       last_line.append("  ");
               if (!compact) 
	          last_line.append("  ");
	    }
	 }
	 

         //loop through ranks then files
	 //forward or back, depending on flip
         for (r=((flipped) ? 1 : b.MAX_RANK), 
	      f=((flipped) ? b.MAX_FILE : 1); 
	      ((flipped) ? r <= b.MAX_RANK : r >= 1); 
	      x = ((flipped) ? r++ : r--)) {

	    sq = b.getSquare(f, r);

            //setup the left coordinates
	    if (left) {
	       if (lowercaseCoords) 
                  out.print(Character.toLowerCase(
		     notation.rankToChar(sq.getRank())));
	       else
                  out.print(Character.toUpperCase(
		     notation.rankToChar(sq.getRank())));

	       out.print(" ");

	       if ((top || bottom) && !compact)
	          out.print(" ");

	       if (!compact)
	          out.print(" ");
	    }

            //loop through the Rank
            for (f=((flipped) ? b.MAX_FILE : 1); 
	         ((flipped) ? f >= 1 : f <= b.MAX_FILE); 
		 x = ((flipped) ? f-- : f++)) {

	       sq = b.getSquare(f, r);
               if (sq.isOccupied()) {
                   c = notation.pieceToChar((ChessPiece) sq.getPiece());
                   if (((ChessPiece)sq.getPiece()).isBlack())
                      c = Character.toLowerCase(c);
                   out.print(c);
               }
               else
                  if (sq.isBlack())
                     out.print(blackSquare);
                  else
                     out.print(whiteSquare);

	       if (!compact && f != ((flipped) ? 1 : b.MAX_FILE))
	          out.print(" ");

               if (bottom && r == ((flipped) ? 1 : b.MAX_RANK)) {
	          if (lowercaseCoords) 
		     last_line.append(Character.toLowerCase(
			notation.fileToChar(sq.getFile())));
		  else
		     last_line.append(Character.toUpperCase(
			notation.fileToChar(sq.getFile())));

		  if (!compact && f != ((flipped) ? 1 : b.MAX_FILE))
		     last_line.append(" ");
	       }

            }

	    if (right) {
	       out.print(" ");

	       if ((top || bottom) && !compact)
	          out.print(" ");

	       if (!compact)
	          out.print(" ");

	       if (lowercaseCoords) 
                  out.print(Character.toLowerCase(
		     notation.rankToChar(sq.getRank())));
	       else
                  out.print(Character.toUpperCase(
		     notation.rankToChar(sq.getRank())));
	    }

	    if (flipped) 
	       f=b.MAX_FILE;
	    else
	       f=1;

            out.println();
         }
         if (bottom) 
	    out.println(last_line.toString());
   }
}
