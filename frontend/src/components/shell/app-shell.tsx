import {
  Sidebar,
} from "./sidebar";

import {
  Topbar,
} from "./topbar";


export function AppShell({

  children,

}: {

  children:
    React.ReactNode;

}) {

  return (

    <div
      className="
        min-h-screen
        lg:flex
      "
    >

      <Sidebar />


      <div
        className="
          min-w-0
          flex-1
        "
      >

        <Topbar />


        <main
          className="
            px-5
            py-7
            md:px-8
            lg:px-10
            lg:py-9
          "
        >

          {
            children
          }

        </main>

      </div>

    </div>

  );

}
