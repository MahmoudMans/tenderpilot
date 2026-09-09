import type {
  Metadata,
} from "next";

import "./globals.css";


export const metadata: Metadata = {

  title: {

    default:
      "TenderPilot",

    template:
      "%s | TenderPilot",

  },


  description:

    "Public procurement intelligence for smarter bid decisions.",

};


export default function RootLayout({

  children,

}: Readonly<{

  children:
    React.ReactNode;

}>) {

  return (

    <html lang="en">

      <body>

        {children}

      </body>

    </html>

  );

}
