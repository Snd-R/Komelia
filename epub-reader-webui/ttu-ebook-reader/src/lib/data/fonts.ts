/**
 * @license BSD-3-Clause
 * Copyright (c) 2024, ッツ Reader Authors
 * All rights reserved.
 */

export interface Font {
  displayName: string;
  familyName: string;
}

export interface UserFont extends Font {
  path: string;
  fileName: string;
}

const fontFamilyIllegalCharsRegex = new RegExp(/[^A-Za-z0-9 ]/)

export function toCanonicalFontFamilyName(name: string): string {
  return name.replace(fontFamilyIllegalCharsRegex, "")
}

export async function loadFont(font: UserFont) {
  const fontFile = new FontFace(font.familyName, `url(${font.path})`)
  return fontFile.load()
      .then((loadedFont) => document.fonts.add(loadedFont))
}


export const defaultSansFont: Font =
    {
      displayName: 'Noto Sans CJK JP',
      familyName: 'Noto Sans CJK JP',
    }

export const defaultSerifFont: Font = defaultSansFont
// {
//   displayName: 'Noto Serif CJK JP',
//   familyName: 'Noto Serif CJK JP'
// }
