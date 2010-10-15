/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package com.aptana.formatter.nodes;

import java.util.Stack;

import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.IFormatterDocument;

public class AbstractFormatterNodeBuilder
{

	private final Stack<IFormatterContainerNode> stack = new Stack<IFormatterContainerNode>();

	protected void start(IFormatterContainerNode root)
	{
		stack.clear();
		stack.push(root);
	}

	protected IFormatterContainerNode peek()
	{
		return stack.peek();
	}

	protected void push(IFormatterContainerNode node)
	{
		addChild(node);
		stack.push(node);
	}

	protected IFormatterNode addChild(IFormatterNode node)
	{
		IFormatterContainerNode parentNode = peek();
		if (!node.isEmpty())
		{
			advanceParent(node, parentNode, node.getStartOffset());
		}
		parentNode.addChild(node);
		return node;
	}

	private void advanceParent(IFormatterNode node, IFormatterContainerNode parentNode, final int pos)
	{
		if (parentNode.getEndOffset() < pos)
		{
			if (node.shouldIgnorePreviousNewLines())
			{
				String text = parentNode.getDocument().get(parentNode.getEndOffset(), pos);
				if (text.trim().length() == 0)
				{
					return;
				}
			}
			parentNode.addChild(createTextNode(parentNode.getDocument(), parentNode.getEndOffset(), pos));

		}
	}

	/**
	 * A utility method that locates the given char in the document, skipping any white-spaces. In case the character
	 * was not found between the given offset and the next non-white-space char, the original offset is returned.
	 * 
	 * @param document
	 *            A {@link FormatterDocument}
	 * @param startOffset
	 *            The start offset of the search
	 * @param c
	 *            The character to search for by scanning the document characters forward from the given start offset
	 *            (including the offset)
	 * @param caseSensitive
	 *            Indicate that the matching of the characters should be done in a case sensitive way or not.
	 * @return The offset of the char; The original offset is returned in case the search for the char failed.
	 */
	protected static int locateCharacterSkippingWhitespaces(FormatterDocument document, int startOffset, char c,
			boolean caseSensitive)
	{
		char toCheck = (caseSensitive) ? c : Character.toLowerCase(c);
		for (int i = startOffset; i < document.getLength(); i++)
		{
			char next = document.charAt(i);
			if (!caseSensitive)
			{
				next = Character.toLowerCase(next);
			}
			if (toCheck == next)
			{
				startOffset = i;
				break;
			}
			if (Character.isWhitespace(next))
			{
				continue;
			}
			break;
		}
		return startOffset;
	}

	protected void checkedPop(IFormatterContainerNode expected, int bodyEnd)
	{
		IFormatterContainerNode top = stack.pop();
		if (top instanceof IFormatterNodeProxy)
		{
			final IFormatterNode target = ((IFormatterNodeProxy) top).getTargetNode();
			if (target instanceof IFormatterContainerNode)
			{
				top = (IFormatterContainerNode) target;
			}
		}
		if (top != expected)
		{
			throw new IllegalStateException();
		}
		if (bodyEnd > 0 && expected.getEndOffset() < bodyEnd)
		{
			expected.addChild(createTextNode(expected.getDocument(), expected.getEndOffset(), bodyEnd));
		}
	}

	/**
	 * @return
	 */
	protected IFormatterTextNode createTextNode(IFormatterDocument document, int startIndex, int endIndex)
	{
		return new FormatterTextNode(document, startIndex, endIndex);
	}
}
