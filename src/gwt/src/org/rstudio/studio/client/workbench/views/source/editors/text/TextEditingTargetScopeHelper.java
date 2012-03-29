/*
 * TextEditingTargetScopeHelper.java
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.source.editors.text;

import org.rstudio.core.client.StringUtil;
import org.rstudio.core.client.regex.Match;
import org.rstudio.core.client.regex.Pattern;
import org.rstudio.core.client.regex.Pattern.ReplaceOperation;
import org.rstudio.studio.client.workbench.views.source.editors.text.ScopeList.ScopePredicate;
import org.rstudio.studio.client.workbench.views.source.editors.text.ace.Position;
import org.rstudio.studio.client.workbench.views.source.editors.text.ace.Range;

public class TextEditingTargetScopeHelper
{
   public TextEditingTargetScopeHelper(DocDisplay docDisplay)
   {
      docDisplay_ = docDisplay;
   }

   public Scope getCurrentSweaveChunk()
   {
      return docDisplay_.getCurrentChunk();
   }

   private class SweaveIncludeContext
   {
      private SweaveIncludeContext()
      {
         scopeList_ = new ScopeList(docDisplay_);
         scopeList_.selectAll(ScopeList.CHUNK);
      }

      public String getSweaveChunkText(final Scope chunk)
      {
         Range range = getSweaveChunkInnerRange(chunk);
         String text = docDisplay_.getCode(range.getStart(), range.getEnd());
         return Pattern.create("^<<(.*?)>>.*").replaceAll(text, new ReplaceOperation()
         {
            @Override
            public String replace(Match m)
            {
               String label = m.getGroup(1).trim();
               Scope included = getScopeByChunkLabel(label,
                                                     chunk.getPreamble());
               if (included == null)
                  return m.getValue();
               else
                  return getSweaveChunkText(included);
            }
         });
      }

      private Scope getScopeByChunkLabel(String label, Position beforeHere)
      {
         for (Scope s : scopeList_.getScopes())
         {
            if (beforeHere != null
                && s.getPreamble().isAfterOrEqualTo(beforeHere))
               return null;
            if (StringUtil.notNull(s.getChunkLabel()).equals(label))
               return s;
         }
         return null;
      }

      private final ScopeList scopeList_;
   }

   public String getSweaveChunkText(Scope chunk)
   {
      return new SweaveIncludeContext().getSweaveChunkText(chunk);
   }

   public Range getSweaveChunkInnerRange(Scope chunk)
   {
      if (chunk == null)
         return null;

      assert chunk.isChunk();

      Position start = Position.create(chunk.getPreamble().getRow() + 1, 0);
      Position end = Position.create(chunk.getEnd().getRow(), 0);
      if (start.getRow() != end.getRow())
      {
         end = Position.create(end.getRow()-1,
                               docDisplay_.getLine(end.getRow()-1).length());
      }
      return Range.fromPoints(start, end);
   }

   public Scope getNextSweaveChunk()
   {
      ScopeList scopeList = new ScopeList(docDisplay_);
      scopeList.selectAll(ScopeList.CHUNK);
      final Position selectionEnd = docDisplay_.getSelectionEnd();
      return scopeList.findFirst(new ScopePredicate()
      {
         @Override
         public boolean test(Scope scope)
         {
            return scope.getPreamble().compareTo(selectionEnd) > 0;
         }
      });
   }

   public Scope getNextFunction(final Position position)
   {
      ScopeList scopeList = new ScopeList(docDisplay_);
      scopeList.selectAll(ScopeList.FUNC);
      return scopeList.findFirst(new ScopePredicate()
      {
         @Override
         public boolean test(Scope scope)
         {
            return scope.getPreamble().compareTo(position) > 0;
         }
      });
   }

   public Scope getPreviousFunction(final Position position)
   {
      ScopeList scopeList = new ScopeList(docDisplay_);
      scopeList.selectAll(ScopeList.FUNC);
      return scopeList.findLast(new ScopePredicate()
      {
         @Override
         public boolean test(Scope scope)
         {
            return scope.getPreamble().compareTo(position) < 0;
         }
      });
   }

   private DocDisplay docDisplay_;
}