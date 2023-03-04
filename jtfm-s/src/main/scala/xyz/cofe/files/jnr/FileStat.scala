package xyz.cofe.files.jnr

import jnr.posix.{FileStat => FStat}

import FileStat._
import java.time.Instant

case class FileStat(
  flags: ModeFlags,
  user: UserID,
  group: GroupID,
  
  accessTime: Instant, // точность до секунды
  modifyTime: Instant, // точность до секунды
  createTime: Instant, // точность до секунды
):
  lazy val fileType : JnrFileType = {
    fileTypeSafe match
      case Left(err) => throw err
      case Right(value) => value    
  }

  lazy val fileTypeSafe: Either[FileStatError,JnrFileType] = {
    if      flags.contains(ModeFlag.NamedPipe) then Right(JnrFileType.NamedPipe)
    else if flags.contains(ModeFlag.Character) then Right(JnrFileType.Character)
    else if flags.contains(ModeFlag.Directory) then Right(JnrFileType.Directory)
    else if flags.contains(ModeFlag.Block)     then Right(JnrFileType.Block)
    else if flags.contains(ModeFlag.Regular)   then Right(JnrFileType.Regular)
    else if flags.contains(ModeFlag.SymLink)   then Right(JnrFileType.SymLink)
    else if flags.contains(ModeFlag.Socket)    then Right(JnrFileType.Socket)
    else Left(FileStatError(flags))
  }

  lazy val filePerm: FilePerm =
    FilePerm(
      stiky = flags.contains(ModeFlag.Stiky),
      suid = flags.contains(ModeFlag.Suid),
      sgid = flags.contains(ModeFlag.Sgid),
      readOwner = flags.contains(ModeFlag.ReadOwner),
      writeOwner = flags.contains(ModeFlag.WriteOwner),
      executeOwner = flags.contains(ModeFlag.ExecuteOwner),
      readGroup = flags.contains(ModeFlag.ReadGroup),
      writeGroup = flags.contains(ModeFlag.WriteGroup),
      executeGroup = flags.contains(ModeFlag.ExecuteGroup),
      readOthers = flags.contains(ModeFlag.ReadOthers),
      writeOthers = flags.contains(ModeFlag.WriteOthers),
      executeOthers = flags.contains(ModeFlag.ExecuteOthers),
    )

object FileStat:
  /////////////////////////////
  class FileStatError(flags:ModeFlags) extends 
    Error(s"undefined file type, mode flags = ${flags.intValue.toBinaryString} bin / ${flags.intValue.toHexString} hex")

  /////////////////////////////
  opaque type ModeFlags = Int
  object ModeFlags:
    def apply(value:Int):ModeFlags = value
    def empty:ModeFlags = 0
    def apply(values:ModeFlag*):ModeFlags =
      values.foldLeft(0){ case(v,f) => v | f.mask }
    
  extension (flags:ModeFlags)
    def intValue:Int = flags
    def contains(flag:ModeFlag):Boolean = flag.in(flags)
    def set(flag:ModeFlag):ModeFlags = flag.set(flags)
    def reset(flag:ModeFlag):ModeFlags = flag.reset(flags)
    def union(otherFlags:ModeFlags*):ModeFlags = 
      otherFlags.foldLeft( intValue ){ case(r,f) => r | f }
    def intersect(otherFlags:ModeFlags*):ModeFlags = 
      otherFlags.foldLeft( intValue ){ case(r,f) => r & f }

  enum ModeFlag( val mask:Int ):
    // type
    case NamedPipe extends ModeFlag(FStat.S_IFIFO)
    case Character extends ModeFlag(FStat.S_IFCHR)
    case Directory extends ModeFlag(FStat.S_IFDIR)
    case Block     extends ModeFlag(FStat.S_IFBLK)
    case Regular   extends ModeFlag(FStat.S_IFREG)
    case SymLink   extends ModeFlag(FStat.S_IFLNK)
    case Socket    extends ModeFlag(FStat.S_IFSOCK)

    //perm
    case Stiky extends ModeFlag(FStat.S_ISVTX)
    case Suid  extends ModeFlag(FStat.S_ISUID)
    case Sgid  extends ModeFlag(FStat.S_ISGID)

    case ReadOwner     extends ModeFlag(FStat.S_IRUSR)
    case WriteOwner    extends ModeFlag(FStat.S_IWUSR)
    case ExecuteOwner  extends ModeFlag(FStat.S_IXUSR)
    case ReadGroup     extends ModeFlag(FStat.S_IRGRP)
    case WriteGroup    extends ModeFlag(FStat.S_IWGRP)
    case ExecuteGroup  extends ModeFlag(FStat.S_IXGRP)
    case ReadOthers    extends ModeFlag(FStat.S_IROTH)
    case WriteOthers   extends ModeFlag(FStat.S_IWOTH)
    case ExecuteOthers extends ModeFlag(FStat.S_IXOTH)

    def in(value:Int):Boolean = (value & mask) == mask
    def set(value:Int):Int = value | mask
    def reset(value:Int):Int = value & (mask ^ (-1))
